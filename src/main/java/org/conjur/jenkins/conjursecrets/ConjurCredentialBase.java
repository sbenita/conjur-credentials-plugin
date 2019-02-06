package org.conjur.jenkins.conjursecrets;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
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
import okhttp3.OkHttpClient;
import org.conjur.jenkins.api.ConjurAPI;
import org.conjur.jenkins.configuration.ConjurConfiguration;
import org.conjur.jenkins.configuration.FolderConjurConfiguration;
import org.conjur.jenkins.configuration.GlobalConjurConfiguration;


public abstract class ConjurCredentialBase extends BaseStandardCredentials {

    private static final long serialVersionUID = 1L;
    
    private transient ConjurConfiguration conjurConfiguration;
    private transient Run<?, ?> context;
    
    private static final Logger LOGGER = Logger.getLogger(ConjurCredentialBase.class.getName());
    
    public ConjurCredentialBase(CredentialsScope scope, String id, String description) {
        super(scope, id, description);
    }
    
    public void setContext(Run<?, ?> context) {
            LOGGER.log(Level.INFO, "Setting context");
            this.context = context;
            setConjurConfiguration(getConfigurationFromContext(context));
    }

    public void setConjurConfiguration(ConjurConfiguration conjurConfiguration) {
            this.conjurConfiguration = conjurConfiguration;
    }

    protected Secret getSecret(String variablePath) {
        String result = "";
        try {
            // Get Conjur Configuration
            ConjurConfiguration config = this.getConjurConfiguration();
            // Get Http Client 
            OkHttpClient client = ConjurAPI.getHttpClient(config);
            // Authenticate to Conjur
            String authToken = ConjurAPI.getAuthorizationToken(client, config, context);
            // Retrieve secret from Conjur
            String secretString = ConjurAPI.getSecret(client, config, authToken, variablePath);
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
    
    protected ConjurConfiguration getConjurConfiguration() {
        if (this.conjurConfiguration == null) {
            if (this.context == null) {
                this.conjurConfiguration = GlobalConjurConfiguration.get().getConjurConfiguration();
            } else {
                this.conjurConfiguration = getConfigurationFromContext(context);
            }
        } 

        return (this.conjurConfiguration);
    }
    
    @SuppressWarnings("unchecked")
    private ConjurConfiguration getConfigurationFromContext(Run<?, ?> context) {
            LOGGER.log(Level.INFO, "Getting Configuration from Context");
            Item job = context.getParent();
            // TODO: validate
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
    
}
