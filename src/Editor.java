import Logic.*;
import java.util.LinkedList;

class Editor
{
    LinkedList<Chip> chipList;

    public Editor()
    {
        chipList = new LinkedList<>();
        createChip("Test", Chip.Defaults.NONE);
        Chip c = getChip("Test");
        c.addChip(new Chip("AND1", Chip.Defaults.AND)).addChip(new Chip("AND2",Chip.Defaults.AND)).addChip(new Chip("AND3",Chip.Defaults.AND));
        c.addOutput("Out");
        for(int i = 0; i < 4; i++) {
            c.addInput("In" + (i + 1));
            c.getInput("In" + (i + 1)).activated = true;
        }
        c.getInput("In1").activated = false;
        c.connectIn("In1", "AND1", "A");
        c.connectIn("In2", "AND1", "B");
        c.connectIn("In3", "AND2", "A");
        c.connectIn("In4", "AND2", "B");
        c.interconnect("AND1", "C", "AND3", "A");
        c.interconnect("AND2", "C", "AND3", "B");
        c.connectOut("AND3", "C", "Out");
        pinOut(c);
        startSimulation(c);
        System.out.println(c.getOutput("Out").activated);
    }

    public void createChip(String name, Chip.Defaults mode)
    {
        Chip c = new Chip(name, mode);
        chipList.add(c);
    }

    public void startSimulation(Chip c) {
        c.callInputLogic();
        runSimulation(c);
    }

    public void runSimulation(Chip c)
    {   
        c.logic();
        for(Chip inner: c.chips) {
            runSimulation(inner);
        }
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
        int longestPinNameL = 0;
        int longestPinNameR = 0;
        for(Pin p: c.iPins) {
            if(p.name.length() >= longestPinNameL) {
                longestPinNameL = p.name.length();
            }
        }
        for(Pin p: c.oPins) {
            if(p.name.length() >= longestPinNameR) {
                longestPinNameR = p.name.length();
            }
        }
        for(int i = 0; i < longestPinNameL; i++) {
            System.out.print(" ");
        }
        System.out.println(" -");

        for (Pin p: c.allPins) {
            int padding = 0;
            if(p.mode == PINMODE.INPUT) {
                padding =    
            }
        }
        for(int i = 0; i < longestPinNameL; i++) {
            System.out.print(" ");
        }
        System.out.println(" -");
    }

    public static void main(String[] args) {
        Editor e = new Editor();
    }
}
