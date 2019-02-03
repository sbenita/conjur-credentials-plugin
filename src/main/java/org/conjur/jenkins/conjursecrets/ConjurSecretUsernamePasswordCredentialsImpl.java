/*
 * The MIT License
 *
 * Copyright 2019 ShloMiri.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.conjur.jenkins.conjursecrets;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Run;
import hudson.util.Secret;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import okhttp3.OkHttpClient;
import org.conjur.jenkins.api.ConjurAPI;
import org.conjur.jenkins.configuration.ConjurConfiguration;
import org.conjur.jenkins.configuration.FolderConjurConfiguration;
import org.conjur.jenkins.configuration.GlobalConjurConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;

public class ConjurSecretUsernamePasswordCredentialsImpl extends BaseStandardCredentials
        implements ConjurSecretUsernamePasswordCredentials{

    private static final long serialVersionUID = 1L;
    
    private String usernameVariablePath; // to be used as Username
    private String passwordVariablePath; // to be used as Password
    
    private transient ConjurConfiguration conjurConfiguration;
    private transient Run<?, ?> context;
    
    private static final Logger LOGGER = Logger.getLogger(ConjurSecretUsernamePasswordCredentials.class.getName());
    
    @DataBoundConstructor
    public ConjurSecretUsernamePasswordCredentialsImpl(@CheckForNull CredentialsScope scope,
                                                   String id,
                                                   @CheckForNull String usernameVariablePath,
                                                   @CheckForNull String passwordVariablePath,
                                                   String description) {
        super(scope, id, description);
        this.usernameVariablePath = usernameVariablePath;
        this.passwordVariablePath = passwordVariablePath;
    }

    @Override
    public String getUsername() {
        return (getSecret(usernameVariablePath).getPlainText());
    }

    @Override
    public Secret getPassword() {
        return (getSecret(passwordVariablePath));
    }
    
    public Secret getSecret(String variablePath) {
        String result = "";
        try {
                // Get Http Client 
                OkHttpClient client = ConjurAPI.getHttpClient(this.conjurConfiguration);
                // Authenticate to Conjur
                String authToken = ConjurAPI.getAuthorizationToken(client, this.conjurConfiguration, context);
                // Retrieve secret from Conjur
                String secretString = ConjurAPI.getSecret(client, this.conjurConfiguration, authToken, variablePath);
                result = secretString;
        } catch (IOException e) {
                Writer writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                String s = writer.toString();
                LOGGER.log(Level.WARNING, "EXCEPTION: " + s);
                result = "EXCEPTION: " + e.getMessage();
        }
        return Secret.fromString(result);
    }

    public void setContext(Run<?, ?> context) {
            LOGGER.log(Level.INFO, "Setting context");
            this.context = context;
            setConjurConfiguration(getConfigurationFromContext(context));
    }

    public void setConjurConfiguration(ConjurConfiguration conjurConfiguration) {
            this.conjurConfiguration = conjurConfiguration;
    }

    @SuppressWarnings("unchecked")
    protected ConjurConfiguration getConfigurationFromContext(Run<?, ?> context) {
            LOGGER.log(Level.INFO, "Getting Configuration from Context");
            Item job = context.getParent();
            ConjurConfiguration conjurConfig = GlobalConjurConfiguration.get().getConjurConfiguration();
            for(ItemGroup<? extends Item> g = job.getParent(); g instanceof AbstractFolder; g = ((AbstractFolder<? extends Item>) g).getParent()  ) {
                    FolderConjurConfiguration fconf = ((AbstractFolder<?>) g).getProperties().get(FolderConjurConfiguration.class);
                    if (!(fconf == null || fconf.getInheritFromParent())) {
                            // take the folder Conjur Configuration
                            conjurConfig = fconf.getConjurConfiguration();
                            break;
                    }
            }
            LOGGER.log(Level.INFO, "<= " + conjurConfig.getApplianceURL());
            return conjurConfig;
    }
    
    public String getDisplayName() {
            return "UP_ConjurSecret:" + this.usernameVariablePath;
    }
    
    @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {

        @Override 
        public String getDisplayName() {
            return "Conjur Username Password Credential";
        }

    }
}
