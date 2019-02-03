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
import org.jenkinsci.plugins.credentialsbinding.BindingDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

@NameWith(value = ConjurSecretUsernamePasswordCredentials.NameProvider.class, priority = 40)
public interface ConjurSecretUsernamePasswordCredentials extends StandardUsernamePasswordCredentials {
    String getDisplayName();
    
    static class NameProvider extends CredentialsNameProvider<ConjurSecretUsernamePasswordCredentials> {
        @Override
        public String getName(ConjurSecretUsernamePasswordCredentials conjurSecretUsernamePasswordCredentials) {
                String description = conjurSecretUsernamePasswordCredentials.getDescription();
                return conjurSecretUsernamePasswordCredentials.getDisplayName()
                                + "/*Conjur*"
                                + " (" + description + ")";
        }

    }
    
    @Extension
    public static class DescriptorImpl extends BindingDescriptor<ConjurSecretUsernamePasswordCredentials> {
		
        @Override
        protected Class<ConjurSecretUsernamePasswordCredentials> type() {
            return ConjurSecretUsernamePasswordCredentials.class;
        }

        @Override
        public String getDisplayName() {
            return "Conjur Username Password Credential";
        }

    }
}
