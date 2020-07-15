package net.ivanhjc.utility.net.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class User {
    private Long id;
    private String username;
    private String realName;
    private Date birthday;
    private String mobile;
    private Integer state; //0-unverified,1-verified,2-suspended,3-deactivated
    private Boolean isVIP;
    private Map<String, Object> addresses;
    private String[] codes;
    private List<Role> roles;
    private BigDecimal money;
    private Role role;

    public User() {
    }

    public User(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Boolean getVIP() {
        return isVIP;
    }

    public void setVIP(Boolean VIP) {
        isVIP = VIP;
    }

    public Map<String, Object> getAddresses() {
        return addresses;
    }

    public void setAddresses(Map<String, Object> addresses) {
        this.addresses = addresses;
    }

    public String[] getCodes() {
        return codes;
    }

    public void setCodes(String[] codes) {
        this.codes = codes;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public Role getRole() {
        return role;
    }

    public static class Role {
        private Integer id;
        private String name;
        private List<Privilege> privileges;
        private Privilege privilege;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Privilege> getPrivileges() {
            return privileges;
        }

        public void setPrivileges(List<Privilege> privileges) {
            this.privileges = privileges;
        }

        public Privilege getPrivilege() {
            return privilege;
        }

    }

    public static class Privilege {
        private Integer id;
        private String name;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
