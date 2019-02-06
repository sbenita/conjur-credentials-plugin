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
import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Run;
import hudson.util.Secret;
import javax.annotation.CheckForNull;
import org.kohsuke.stapler.DataBoundConstructor;

@NameWith(value = ConjurUsernamePasswordCredentials.NameProvider.class)
public class ConjurUsernamePasswordCredentials extends ConjurCredentialBase
        implements StandardUsernamePasswordCredentials {
    
    private static final long serialVersionUID = 1L;
    
    private String usernameVariablePath; // to be used as Username    
    private String passwordVariablePath; // to be used as Password
    
     @DataBoundConstructor
    public ConjurUsernamePasswordCredentials(@CheckForNull CredentialsScope scope,
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
    
    public String getDisplayName() {
            return "Conjur:" + this.usernameVariablePath;
    }

    @Extension(ordinal = 1)
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        @Override
        public String getDisplayName() {
            return "Conjur Username Password Credential";
        }
    }    
        
    static class NameProvider extends CredentialsNameProvider<ConjurUsernamePasswordCredentials> {
        
        @Override
        public String getName(ConjurUsernamePasswordCredentials conjurUsernamePasswordCredentials) {
                String description = conjurUsernamePasswordCredentials.getDescription();
                return conjurUsernamePasswordCredentials.getDisplayName()
                                + "/*Conjur*"
                                + " (" + description + ")";
        }

    }
}
