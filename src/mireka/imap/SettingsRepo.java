package mireka.imap;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import mireka.ConfigurationException;
import mireka.login.CiUsername;
import mireka.login.User;
import mireka.login.UserConfig;
import mireka.login.UserRepo;
import mireka.maildata.type.AddrSpec;
import mireka.maildata.type.DomainPart;
import mireka.maildata.type.LocalPart;

public class SettingsRepo {

    private UserRepo users;

    public Settings get(User username) {
        UserConfig user = users.get(username);
        return new Settings() {
            @Override
            public User user() {
                return user.name;
            }

            @Override
            public String name() {
                return user.name.name();
            }

            @Override
            public CiUsername ciName() {
                return user.ciName();
            }

            @Override
            public String password() {
                return user.password;
            }

            @Override
            public boolean loginDisabled() {
                return user.loginDisabled;
            }

            @Override
            public boolean hasPersonalNamespace() {
                return user.imapHasPersonalNamespace;
            }

            @Override
            public long getQuota() {
                return 0;
            }

            @Override
            public DomainPart internalDomain() {
                return DomainPart.parse(user.internalDomain);
            }

            @Override
            public AddrSpec internalAddress() {
                try {
                    if (user.internalAddress == null) {
                        AddrSpec addr = new AddrSpec();
                        addr.localPart = new LocalPart(name());
                        addr.domain = internalDomain();
                        return addr;
                    } else {
                        return AddrSpec.fromString(user.internalAddress);
                    }
                } catch (ParseException e) {
                    throw new ConfigurationException();
                }
            }

            @Override
            public String internalName() {
                return Objects.requireNonNullElse(user.internalName, name());
            }

            @Override
            public String postmasterName() {
                return Objects.requireNonNullElse(user.postmasterName, "Postmaster");
            }

            @Override
            public AddrSpec postmasterAddress() {
                try {
                    if (user.postmasterAddress == null) {
                        AddrSpec addr = new AddrSpec();
                        addr.localPart = new LocalPart("postmaster");
                        addr.domain = internalDomain();
                        return addr;
                    } else {
                        return AddrSpec.fromString(user.internalAddress);
                    }
                } catch (ParseException e) {
                    throw new ConfigurationException();
                }
            }

            @Override
            public Optional<Path> sieveScript() {
                return Optional.ofNullable(user.sieveScript)
                        .map(p -> FileSystems.getDefault().getPath(p));
            }

            @Override
            public Locale locale() {
                return Locale.forLanguageTag(Objects.requireNonNullElse(user.locale, "en-US"));
            }

        };
    }

    @Inject
    public void setUsers(UserRepo users) {
        this.users = users;
    }
}
