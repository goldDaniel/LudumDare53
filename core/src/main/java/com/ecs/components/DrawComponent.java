package com.ecs.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.core.RenderResources;
import com.ecs.Component;

public class DrawComponent extends Component
{
    public final Color currentColor = Color.WHITE.cpy();

    public final TextureRegion texture = new TextureRegion(RenderResources.getTexture("textures/default.png"));

    public final Vector2 scale = new Vector2(1.0f, 1.0f);

    public float rotation = 0;

    public boolean flipX = false;
    public boolean flipY = false;
}
