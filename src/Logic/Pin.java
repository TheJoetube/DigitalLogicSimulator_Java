package Logic;

public class Pin
{
    enum PINMODE {
        INPUT,
        OUTPUT
    }

    public String name;
    PINMODE mode;
    public boolean activated;
    Pin connection = null;
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
