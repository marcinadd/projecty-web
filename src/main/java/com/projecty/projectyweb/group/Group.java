package com.projecty.projectyweb.group;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

@MappedSuperclass
@Getter
@Setter
public class Group {
    @Id
    @GeneratedValue
    protected Long id;

    @NotBlank
    protected String name;

    @Transient
    protected List<String> usernames;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    protected Date modifyDate;
}
