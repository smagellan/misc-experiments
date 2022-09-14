package smagellan.test;

import javax.security.auth.callback.*;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginContext;
import javax.security.sasl.RealmCallback;
import java.io.IOException;
import java.util.Map;

public class KerberosTest {
    static {
        System.setProperty("sun.security.krb5.debug", "true");
    }

    public static void main(String[] args) throws Exception {
        AppConfigurationEntry entry = new AppConfigurationEntry(
                "com.sun.security.auth.module.Krb5LoginModule",
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                Map.of("debug", "true"));
        //sun.security.krb5.Config.getInstance();
        //--add-exports java.security.jgss/sun.security.krb5=ALL-UNNAMED
        LoginContext ctx = new LoginContext("", null, new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback cb : callbacks) {
                    if (cb instanceof NameCallback) {
                        ((NameCallback) cb).setName("name");
                    } else if (cb instanceof PasswordCallback) {
                        ((PasswordCallback) cb).setPassword("pwd".toCharArray());
                    } else if (cb instanceof RealmCallback) {
                        ((RealmCallback) cb).setText("realm");
                    }
                }
            }
        }, new javax.security.auth.login.Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return new AppConfigurationEntry[] { entry };
            }
        });

        ctx.login();
    }
}
