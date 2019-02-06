package org.conjur.jenkins.conjursecrets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.annotation.CheckForNull;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.conjur.jenkins.api.ConjurAPI;
import org.conjur.jenkins.configuration.ConjurConfiguration;
import org.conjur.jenkins.configuration.FolderConjurConfiguration;
import org.conjur.jenkins.configuration.GlobalConjurConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Run;
import hudson.util.Secret;
import okhttp3.OkHttpClient;

public class ConjurSecretCredentialsImpl extends ConjurCredentialBase implements ConjurSecretCredentials {

    private static final long serialVersionUID = 1L;

    private String variablePath; // to be used as Username

    private static final Logger LOGGER = Logger.getLogger( ConjurSecretCredentialsImpl.class.getName());

    @DataBoundConstructor
    public ConjurSecretCredentialsImpl(@CheckForNull CredentialsScope scope, 
                                               @CheckForNull String id,
                                               @CheckForNull String variablePath,
                                               @CheckForNull String description) {
            super(scope, id, description);
            this.variablePath = variablePath;
    }

    
    public String getVariablePath() {
            return this.variablePath;
    }
	
    @DataBoundSetter
    public void setVariablePath(String variablePath) {
            this.variablePath = variablePath;
    }

    @Override
    public String getDisplayName() {
            return "ConjurSecret:" + this.variablePath;
    }

    @Override
    public Secret getSecret() {
        return (super.getSecret(this.variablePath));
    }
	
   @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {

        @Override 
        public String getDisplayName() {
            return "Conjur Secret Credential";
        }
    }
}
