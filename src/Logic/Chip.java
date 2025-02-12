package Logic;
import java.util.ArrayList;

public class Chip
{
    public enum Defaults {
        NONE,
        AND,
        OR,
        NOT
    }

    public String name;
    Defaults dMode = Defaults.NONE;
    public ArrayList<Chip> chips;
    public ArrayList<Pin> iPins;
    public ArrayList<Pin> oPins;
    public ArrayList<Pin> allPins;
    boolean editable = true;

    public Chip(String pName)
    {
        name = pName;
        chips = new ArrayList<>();
        iPins = new ArrayList<>();
        oPins = new ArrayList<>();
        allPins = new ArrayList<>();
    }

    public Chip(String pName, Defaults mode)
    {
        name = pName;
        chips = new ArrayList<>();
        iPins = new ArrayList<>();
        oPins = new ArrayList<>();
        allPins = new ArrayList<>();
        setDefault(mode);
    }

    public Chip logic()
    {
        switch(dMode) {
            case Defaults.AND:
                oPins.getLast().activated = (iPins.getFirst().activated && iPins.getLast().activated);
                break;

            case Defaults.NOT:
                oPins.getLast().activated = !iPins.getFirst().activated;
                break;

            case Defaults.OR:
                oPins.getLast().activated = (iPins.getFirst().activated || iPins.getLast().activated);
                break;

            case Defaults.NONE:
                break;
        }
        return this;
    }

    public Chip setDefault(Defaults d) {
        dMode = d;
        switch(dMode) {
            case Defaults.AND, Defaults.OR:
                editable = false;
                this.addInput("A");
                this.addInput("B");
                this.addOutput("C");
                break;

            case Defaults.NOT:
                editable = false;
                this.addInput("A");
                this.addOutput("B");
                break;

            case Defaults.NONE:
                break;
        }
        return this;
    }

    public Chip connectIn(String startPin, String endChip, String endPin)
    {
        Pin start = getPin(startPin);
        for(Pin p: getChip(endChip).iPins) {
            if(p.name.equals(endPin)) {
                start.connection = p;
                return this;
            }
        }
        System.out.println("Couldn't connect Pins");
        return this;
    }

    public Chip connectOut(String startChip, String startPin, String endPin)
    {
        Chip start = getChip(startChip);
        for(Pin p: oPins) {
            if(p.name.equals(endPin)) {
                start.getOutput(startPin).connection = p;
                return this;
            }
        }
        System.out.println("Couldn't connect Pins");
        return this;
    }

    public Chip interconnect(String startChip, String startPin, String endChip, String endPin)
    {
        Chip start = getChip(startChip);
        Chip end = getChip(endChip);
        for(Pin p: end.iPins) {
            if(p.name.equals(endPin)) {
                start.getOutput(startPin).connection = end.getInput(endPin);
                return this;
            }
        }
        System.out.println("Couldn't connect Pins");
        return this;
    }

    public Chip addInput(String name)
    {
        Pin p = new Pin(Pin.PINMODE.INPUT, name);
        p.setChip(this);
        iPins.add(p);
        allPins.add(p);
        return this;
    }

    public Chip addOutput(String name)
    {
        Pin p = new Pin(Pin.PINMODE.OUTPUT, name);
        p.setChip(this);
        oPins.add(p);
        allPins.add(p);
        return this;
    }

    public Chip addChip(Chip chip)
    {
        chips.add(chip);
        return this;
    }

    public Chip getChip(String name)
    {
        if(name == null) {
            return this;
        }
        for(Chip c: chips) {
            if(c.name.equals(name)) {
                return c;
            }
        }
        throw new RuntimeException("No Chip with name: " + name);
    }

    public Pin getInput(String name)
    {
        for(Pin p: iPins) {
            if(p.name.equals(name)) {
                return p;
            }
        }
        throw new RuntimeException("No input with name: " + name);
    }

    public Pin getOutput(String name)
    {
        for(Pin p: oPins) {
            if(p.name.equals(name)) {
                return p;
            }
        }
        throw new RuntimeException("No output with name: " + name);
    }

    public Pin getPin(String name)
    {
        for(Pin p: allPins) {
            if(p.name.equals(name)) {
                return p;
            }
        }
        throw new RuntimeException("No pin with name: " + name);
    }

    public void activateInput(String name, boolean a)
    {
        getInput(name).setActivated(a);
    }
}
