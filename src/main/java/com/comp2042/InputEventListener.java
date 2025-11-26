package com.comp2042;

public interface InputEventListener {

    DownData onDownEvent(MoveEvent event);

    DownData onHardDropEvent(MoveEvent event);

    ViewData onLeftEvent(MoveEvent event);

    ViewData onRightEvent(MoveEvent event);

    ViewData onRotateEvent(MoveEvent event);

    void createNewGame();

    ViewData getGhostPosition(ViewData currentBrick);
}
