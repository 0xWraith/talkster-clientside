package com.client.talkster.classes.theme;

import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

public class ButtonElements
{
    private final List<Button> buttons;
    private final List<Boolean> buttonRounded;
    private final List<ImageButton> imageButtons;
    private final List<Boolean> imageButtonRounded;

    public ButtonElements()
    {
        buttons = new ArrayList<>();
        imageButtons = new ArrayList<>();
        buttonRounded = new ArrayList<>();
        imageButtonRounded = new ArrayList<>();
    }

    public void addButton(Button button, boolean isRounded)
    {
        this.buttons.add(button);
        this.buttonRounded.add(isRounded);
    }

    public void addImageButton(ImageButton imageButton, boolean isRounded)
    {
        this.imageButtons.add(imageButton);
        this.imageButtonRounded.add(isRounded);
    }

    public List<Button> getButtons() { return buttons; }
    public List<ImageButton> getImageButtons() { return imageButtons; }
    public boolean isButtonRounded(int index) { return buttonRounded.get(index); }
    public boolean isImageButtonRounded(int index) { return imageButtonRounded.get(index); }
}
