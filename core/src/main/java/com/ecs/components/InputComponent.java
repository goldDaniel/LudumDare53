package com.ecs.components;

import com.badlogic.gdx.Input;
import com.ecs.Component;

public class InputComponent extends Component
{
    public int left = Input.Keys.A;
    public int right = Input.Keys.D;
    public int up = Input.Keys.W;
    public int down =  Input.Keys.S;

    public int cwRotate = Input.Keys.Q;

    public int ccwRotate = Input.Keys.E;

    public int action = Input.Keys.SPACE;
}
