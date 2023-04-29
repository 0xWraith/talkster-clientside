package com.client.talkster.classes.chat;

import android.graphics.Bitmap;

import com.client.talkster.classes.User;
import com.client.talkster.classes.chat.Chat;
import com.client.talkster.utils.enums.EChatType;

import java.util.ArrayList;
import java.util.List;

public class GroupChat extends Chat
{

    private long ownerID;
    private String groupName;
    private String groupDescription;
    private Bitmap groupImage;
    private List<User> groupMembers;

    public GroupChat()
    {
        super(EChatType.GROUP_CHAT);
        groupMembers = new ArrayList<>();
    }

    public long getOwnerID() { return ownerID; }
    public String getGroupName() { return groupName; }
    public List<User> getGroupMembers() { return groupMembers; }
    public Bitmap getGroupImage() { return groupImage; }
    public String getGroupDescription() { return groupDescription; }

    public void setOwnerID(long ownerID) { this.ownerID = ownerID; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public void setGroupImage(Bitmap groupImage) { this.groupImage = groupImage; }
    public void addGroupMember(User groupMember) { this.groupMembers.add(groupMember); }
    public void setGroupMembers(List<User> groupMembers) { this.groupMembers = groupMembers; }
    public void setGroupDescription(String groupDescription) { this.groupDescription = groupDescription; }

    @Override
    public String toString()
    {

        return "GroupChat{" +
                "chat=" + super.toString() +
                "ownerID=" + ownerID +
                ", groupName='" + groupName + '\'' +
                ", groupDescription='" + groupDescription + '\'' +
                ", groupImage=" + groupImage +
                ", groupMembers=" + groupMembers +
                '}';
    }
}
