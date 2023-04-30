package com.ecs.components;

import com.badlogic.gdx.Input;
import com.ecs.Component;

public class InputComponent extends Component
{
    public int left = Input.Keys.LEFT;
    public int right = Input.Keys.RIGHT;
    public int up = Input.Keys.Z;
    public int down =  Input.Keys.DOWN;

    public int action = Input.Keys.X;
}
