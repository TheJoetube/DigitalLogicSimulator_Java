package Logic;

import java.util.ArrayList;

public class Pin
{
    enum PINMODE {
        INPUT,
        OUTPUT
    }

    public String name;
    public PINMODE mode;
    public boolean activated;
    Pin connectionIn = null;
    public ArrayList<Pin> connectionsOut = new ArrayList<>();
    Chip chip;

    public Pin(PINMODE mode)
    {
        name = "Unnamed";
        this.mode = mode;
        activated = false;
    }

    public Pin(PINMODE mode, String pName)
    {
        name = pName;
        this.mode = mode;
        activated = false;
    }

    public void setChip(Chip c) {
        chip = c;
    }

    public void setActivated(boolean activated)
    {
        this.activated = activated;
    }
}
