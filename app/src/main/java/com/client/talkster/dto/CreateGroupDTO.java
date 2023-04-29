package com.client.talkster.dto;

import java.util.List;

public class CreateGroupDTO
{
    private final long id;
    private final String groupname;
    private final List<Long> members;

    public CreateGroupDTO(long id, String groupname, List<Long> members)
    {
        this.id = id;
        this.members = members;
        this.groupname = groupname;
    }

    public long getId() { return id; }
    public String getgroupname() { return groupname; }
    public List<Long> getMembers() { return members; }

}