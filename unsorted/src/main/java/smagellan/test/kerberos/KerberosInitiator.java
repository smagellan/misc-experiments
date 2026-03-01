package smagellan.test.kerberos;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.Subject;
import java.util.Map;

public class KerberosInitiator {
    static {
        System.setProperty("java.security.krb5.conf", "/home/vladimir/projects/misc-experiments/unsorted/src/main/resources/smagellan/test/kerberos/krb5.conf");
        //System.setProperty("java.security.auth.login.config", "login.conf");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("sun.security.krb5.debug", "true");

        //System.setProperty("java.security.krb5.realm", "myrealm");
        //System.setProperty("java.security.krb5.kdc", "kdc");
        //System.setProperty("http.auth.preference", "SSPI");
        //System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    }

    private final javax.security.auth.login.Configuration conf;
    private javax.security.auth.login.LoginContext ctxt;

    public KerberosInitiator() {
        Map<String, String> conf = Map.of(
                "debug", "true",
                //"useTicketCache", "true",
                //"renewTGT", "true",
                "useKeyTab", "true",
                "storeKey", "true",
                "refreshKrb5Config", "true",
                //https://github.com/criteo/kerberos-docker/blob/f55539cacb7e1ef5d3c1f5feacbd521fa8c04131/gssapi-java/gss-server/config/jaas-krb5.conf#L4
                "principal", "host/krb5-service-example-com.example.com@EXAMPLE.COM",
                //to init the file run: docker exec -it krb5-service-example-com cat /etc/krb5.keytab > /tmp/krb5.keytab
                "keyTab", "/tmp/krb5.keytab"
        );
        AppConfigurationEntry[] entries = new AppConfigurationEntry[] {
                new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, conf)
        };

        this.conf = new javax.security.auth.login.Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return entries;
            }
        };
    }

    public LoginContext doLogin() throws LoginException {
        if (ctxt != null) {
            ctxt.logout();
            ctxt = null;
        }
        ctxt = new javax.security.auth.login.LoginContext("KrbLogin", null,
                cbList -> {
                    for (javax.security.auth.callback.Callback handler : cbList) {
                        if (handler instanceof javax.security.auth.callback.NameCallback nmCb) {
                            nmCb.setName("bob@EXAMPLE.COM");
                        }
                        if (handler instanceof javax.security.auth.callback.PasswordCallback pwdCb) {
                            pwdCb.setPassword("pwd".toCharArray());
                        }
                    }
                },
                conf
        );
        ctxt.login();
        System.err.println("tickets: " + ctxt.getSubject().getPrivateCredentials(javax.security.auth.kerberos.KerberosTicket.class));
        return ctxt;
    }

    public static void main(String[] args) throws LoginException {
        KerberosInitiator initiator = new KerberosInitiator();
        LoginContext loginContext = initiator.doLogin();
        Subject.callAs(loginContext.getSubject(), () -> Boolean.TRUE);
    }
}
