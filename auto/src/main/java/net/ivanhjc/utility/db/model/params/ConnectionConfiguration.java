package net.ivanhjc.utility.db.model.params;

public class ConnectionConfiguration {
    /**
     * A user-provided name of the connection, which can be used to identify which connection to use.
     */
    private String name;
    /**
     * JDBC url
     */
    private String url;
    /**
     * Database connection username
     */
    private String username;
    /**
     * Database connection password
     */
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
