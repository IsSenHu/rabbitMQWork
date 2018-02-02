package com.husen.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author 11785
 */
@Entity(name = "t_talk_user")
public class TalkUser {
    @Id
    private String nickName;
    @Column
    private String password;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "TalkUser{" +
                "nickName='" + nickName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
