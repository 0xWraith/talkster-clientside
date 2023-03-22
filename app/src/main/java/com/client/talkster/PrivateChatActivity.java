package com.client.talkster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.client.talkster.adapters.ChatMessagesAdapter;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.Message;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.utils.enums.MessageType;

import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;

public class PrivateChatActivity extends AppCompatActivity implements IActivity
{

    private Chat chat;
    private UserJWT userJWT;
    private TextView userNameText;
    private Button chatSendButton;
    private EditText chatInputText;
    private TextView userStatusText;
    private RecyclerView chatMessagesList;
    private ChatMessagesAdapter chatMessagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        Bundle bundle = getIntent().getExtras();

        chat = (Chat) bundle.get("chat");
        userJWT = (UserJWT) bundle.get("userJWT");

        userNameText = findViewById(R.id.userNameText);
        chatInputText = findViewById(R.id.chatInputText);
        chatSendButton = findViewById(R.id.chatSendButton);
        userStatusText = findViewById(R.id.userStatusText);
        chatMessagesList = findViewById(R.id.chatMessagesList);

        userNameText.setText(chat.getReceiverName());
        userStatusText.setText("last seen at 12:35");

        chatMessagesAdapter = new ChatMessagesAdapter(chat.getMessages(), userJWT.getID(), this);
        chatMessagesList.setAdapter(chatMessagesAdapter);

        chatSendButton.setOnClickListener(view ->
        {
            String message = chatInputText.getText().toString().trim();
            int length = message.length();

            if(length == 0 || length > 4097)
                return;

            MessageDTO messageDTO = new MessageDTO();

            messageDTO.setchatid(chat.getId());
            messageDTO.setmessagecontent(message);
            messageDTO.setsenderid(userJWT.getID());
            messageDTO.setjwttoken(userJWT.getJWTToken());
            messageDTO.setreceiverid(chat.getReceiverID());
            messageDTO.setmessagetype(MessageType.TEXT_MESSAGE);
            messageDTO.setmessagetimestamp(OffsetDateTime.now().toString());

            Message newMessage = new ModelMapper().map(messageDTO, Message.class);

            chatMessagesAdapter.getMessages().add(newMessage);
            chatMessagesAdapter.notifyItemInserted(chatMessagesAdapter.getMessages().size() - 1);

        });
    }
}