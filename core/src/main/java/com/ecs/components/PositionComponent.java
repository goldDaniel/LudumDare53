package com.ecs.components;

import com.badlogic.gdx.math.Vector2;
import com.ecs.Component;

public class PositionComponent extends Component
{
    public Vector2 position = new Vector2();
    public Vector2 previousPosition = new Vector2();
}
