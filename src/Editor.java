import Logic.*;
import java.util.LinkedList;

class Editor
{
    LinkedList<Chip> chipList;
    public Chip ogChip;

    public Editor()
    {
        chipList = new LinkedList<>();

        createChip("2AND", Chip.Defaults.NONE);
        Chip c = getChip("2AND");
        c.addChip(new Chip("AND1", Chip.Defaults.AND)).addChip(new Chip("AND2",Chip.Defaults.AND)).addChip(new Chip("AND3",Chip.Defaults.AND));
        c.addOutput("Out");
        for(int i = 0; i < 4; i++) {
            c.addInput("In" + (i + 1));
            //c.getInput("In" + (i + 1)).activated = true;
        }
        c.connectIn("In1", "AND1", "A");
        c.connectIn("In2", "AND1", "B");
        c.connectIn("In3", "AND2", "A");
        c.connectIn("In4", "AND2", "B");
        c.interconnect("AND1", "C", "AND3", "A");
        c.interconnect("AND2", "C", "AND3", "B");
        c.connectOut("AND3", "C", "Out");

        createChip("Test", Chip.Defaults.NONE);
        Chip d = getChip("Test");
        d.addChip(getChip("2AND")).addChip(new Chip("AND1", Chip.Defaults.AND));
        d.addOutput("Out");
        for(int i = 0; i < 5; i++) {
            d.addInput("In" + (i + 1));
            d.getInput("In" + (i + 1)).activated = true;
        }
        d.connectIn("In1", "2AND", "In1");
        d.connectIn("In2", "2AND", "In2");
        d.connectIn("In3", "2AND", "In3");
        d.connectIn("In4", "2AND", "In4");
        d.connectIn("In5", "AND1", "B");
        d.interconnect("2AND", "Out", "AND1", "A");
        d.connectOut("AND1", "C", "Out");
        runSimulation(d);
        pinOut(getChip("2AND"));
        System.out.println(d.getOutput("Out").activated);
    }

    public void createChip(String name, Chip.Defaults mode)
    {
        Chip c = new Chip(name, mode);
        chipList.add(c);
    }

    public Chip getChip(String name)
    {
        for(Chip c: chipList) {
            if(c.name.equals(name)) {
                return c;
            }
        }
        throw new RuntimeException("No Chip with name: " + name);
    }

    public void runSimulation(Chip c)
    {
        if(c.getDMode() == Chip.Defaults.NONE) {
            for(Pin p: c.iPins) {
                for(Pin connected: p.connectionsOut) {
                    connected.activated = p.activated;
                }
            }
            for(Pin p: c.iPins) {
                for(Pin connected: p.connectionsOut) {
                    runSimulation(connected.chip);
                }
            }
            for(Pin p: c.oPins) {
                for(Pin connected: p.connectionsOut) {
                    connected.activated = p.activated;
                }
            }
        } else {
            c.logic();
            for(Pin p: c.oPins) {
                for(Pin connected: p.connectionsOut) {
                    connected.activated = p.activated;
                }
            }
            for(Pin p: c.oPins) {
                for(Pin connected: p.connectionsOut) {
                    if(connected.mode != Pin.PINMODE.OUTPUT) {
                        runSimulation(connected.chip);
                    }
                }
            }
        }
        /*if(c.getDMode() == Chip.Defaults.NONE) {
            for(Pin in: c.iPins) {
                for(Pin p: in.connectionsOut) {
                    p.activated = in.activated;
                }
                for(Pin p: in.connectionsOut) {
                    runSimulation(p.chip);
                }
            }
        } else {
            c.logic();
        }
        for(Pin out: c.oPins) {
            for(Pin p: out.connectionsOut) {
                p.activated = out.activated;
            }
            for(Pin p: out.connectionsOut) {
                runSimulation(p.chip);
            }
        }*/
    }

    public void pinOutOld(Chip c)
    {
        System.out.println("IN:");
        for(Pin p: c.iPins) {
            System.out.print(p.name);
            if(p.connectionsOut.size() >= 1) {
                System.out.print("->");
            }
            for(Pin po: p.connectionsOut) {
                System.out.print(" " + po.name);
                if(p.connectionsOut.size() > 1) {
                    System.out.println(" |");
                }
            }
            System.out.println("");
        }
        System.out.println("\nOUT:");
        for(Pin p: c.oPins) {
            System.out.println(p.name);
        }
    }

    public void pinOut(Chip c) {
        System.out.println(c.name + ":");
        int longestPinNameL = 0;
        for(Pin p: c.iPins) {
            if(p.name.length() >= longestPinNameL) {
                longestPinNameL = p.name.length();
            }
        }
        for(int i = 0; i < longestPinNameL + 4; i++) {
            System.out.print(" ");
        }
        System.out.print(" -");

        for(int i = 0; i < Math.max(c.iPins.size(), c.oPins.size()); i++) {
            int padding;
            if(i < c.iPins.size()) {
                padding = longestPinNameL - c.iPins.get(i).name.length();
                for(int j = 0; j < padding; j++) {
                    System.out.print(" ");
                }
                System.out.println();
                System.out.print(c.iPins.get(i).name + " -> ");
                System.out.print("| |");
            }
            if(i < c.oPins.size()) {
                System.out.print(" <- " + c.oPins.get(i).name);
            }
        }
        System.out.println();
        for(int i = 0; i < longestPinNameL + 4; i++) {
            System.out.print(" ");
        }
        System.out.println(" -");
    }

    public static void main(String[] args) {
        Editor e = new Editor();
    }
}
