import Logic.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.IOException;
import java.util.LinkedList;

class Editor
{
    LinkedList<Chip> chipList;

    public Editor()
    {
        chipList = new LinkedList<>();

        createChip("XOR", Chip.Defaults.NONE);
        Chip x = getChip("XOR");
        x.addChip(new Chip("AND1", Chip.Defaults.AND)).addChip(new Chip("AND2", Chip.Defaults.AND)).addChip(new Chip("OR", Chip.Defaults.OR)).addChip(new Chip("NOT", Chip.Defaults.NOT));
        x.addInput("A");
        x.addInput("B");
        x.getInput("A").activated = true;
        //x.getInput("B").activated = true;
        x.addOutput("C");

        x.connectIn("A", "OR", "A");
        x.connectIn("B", "OR", "B");
        x.connectIn("A", "AND1", "A");
        x.connectIn("B", "AND1", "B");
        x.interconnect("AND1", "C", "NOT", "A");
        x.interconnect("OR", "C", "AND2", "A");
        x.interconnect("NOT", "B", "AND2", "B");
        x.connectOut("AND2", "C", "C");

        runSimulation(x);
        pinOut(x);
        System.out.println(x.getOutput("C").activated);

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

        runSimulation(c);
        pinOut(c);
        System.out.println(c.getOutput("Out").activated);

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
        pinOut(d);
        System.out.println(d.getOutput("Out").activated);

        saveChipToFile(x);
        Chip e = loadFromJson("XOR");
    }

    public void saveChipToFile(Chip c) {
        JSONObject json = new JSONObject();
        json.put("name", c.name);

        JSONArray inputs = new JSONArray();
        JSONArray outputs = new JSONArray();

        for(Pin p: c.iPins) {
            inputs.add(p.name);
        }

        for(Pin p: c.oPins) {
            outputs.add(p.name);
        }

        json.put("inputs", inputs);
        json.put("outputs", outputs);

        JSONArray chipList = new JSONArray();

        for(Chip internal: c.chips) {
            JSONObject chip = new JSONObject();

            chip.put("name", internal.name);
            chip.put("type", internal.getDMode().toString());

            JSONArray cInputs = new JSONArray();
            JSONArray cOutputs = new JSONArray();

            for(Pin p: internal.iPins) {
                cInputs.add(p.name);
            }

            for(Pin p: internal.oPins) {
                cOutputs.add(p.name);
            }

            chip.put("inputs", cInputs);
            chip.put("outputs", cOutputs);

            chipList.add(chip);
        }

        json.put("internal_chips", chipList);

        JSONArray connections = new JSONArray();

        for (Pin pin : c.allPins) {
            for (Pin connectedPin : pin.connectionsOut) {
                JSONObject connection = new JSONObject();

                // Format: "ChipName:PinName"
                String from = pin.chip.name + ":" + pin.name;
                String to = connectedPin.chip.name + ":" + connectedPin.name;

                connection.put("from", from);
                connection.put("to", to);

                connections.add(connection);
            }
        }

        for(Chip internal: c.chips) {
            for (Pin pin : internal.allPins) {
                for (Pin connectedPin : pin.connectionsOut) {
                    JSONObject connection = new JSONObject();

                    // Format: "ChipName:PinName"
                    String from = pin.chip.name + ":" + pin.name;
                    String to = connectedPin.chip.name + ":" + connectedPin.name;

                    connection.put("from", from);
                    connection.put("to", to);

                    connections.add(connection);
                }
            }
        }

        json.put("connections", connections);

        try {
            FileWriter file = new FileWriter("./Chips/" + c.name + ".json");
            file.write(json.toJSONString());
            file.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public Chip loadFromJson(String fileName) {
        JSONParser parser = new JSONParser();
        Reader reader;
        JSONObject json = null;
        try 
        {
            reader = new FileReader("./Chips/" + fileName + ".json");
            json = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } 
        createChip((String) json.get("name"), Chip.Defaults.NONE);

        return getChip((Sring) json.get("name"));
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
