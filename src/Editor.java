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
import java.util.ArrayList;
import java.util.LinkedList;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;

class Editor
{
    LinkedList<Chip> chipList;

    public Editor()
    {
        chipList = new LinkedList<>();

        //saveChipToFile(d);
        Chip e = loadFromJson("Test");
        for(int i = 0; i < 5; i++) {
            e.getInput("In" + (i + 1)).activated = true;
        }
        //runSimulation(e);
        pinOut(e);
        //System.out.println(e.getOutput("Out").activated);
        printTruthTable(e);
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

        for (Pin pin : c.iPins) {
            for (Pin connectedPin : pin.connectionsOut) {
                JSONObject connection = new JSONObject();

                // Format: "ChipName:PinName"
                String from;
                String to;

                if(pin.mode == Pin.PINMODE.INPUT) {
                    from = pin.chip.name + ":" + pin.name;
                    to = connectedPin.chip.name + ":" + connectedPin.name;
                } else {
                    from = connectedPin.chip.name + ":" + connectedPin.name;
                    to = pin.chip.name + ":" + pin.name;
                }

                connection.put("from", from);
                connection.put("to", to);

                connections.add(connection);

            }
        }

        for(Chip internal: c.chips) {
            for (Pin pin : internal.oPins) {
                for (Pin connectedPin : pin.connectionsOut) {
                    JSONObject connection = new JSONObject();

                    // Format: "ChipName:PinName"
                    String from;
                    String to;

                    if(pin.mode == Pin.PINMODE.OUTPUT) {
                        from = pin.chip.name + ":" + pin.name;
                        to = connectedPin.chip.name + ":" + connectedPin.name;
                    } else {
                        from = connectedPin.chip.name + ":" + connectedPin.name;
                        to = pin.chip.name + ":" + pin.name;
                    }

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
        Reader reader = null;
        JSONObject json = null;
        String name;
        try 
        {
            reader = new FileReader("./Chips/" + fileName + ".json");
            json = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } 
        name = (String) json.get("name");
        createChip(name, Chip.Defaults.NONE);

        JSONArray inputs = (JSONArray) json.get("inputs");
        JSONArray outputs = (JSONArray) json.get("outputs");

        for(Object s: inputs.toArray()) {
            getChip(name).addInput(s.toString());
        }
        for(Object s: outputs.toArray()) {
            getChip(name).addOutput(s.toString());
        }

        JSONArray chipsArr = (JSONArray) json.get("internal_chips");
        JSONObject[] chips = new JSONObject[chipsArr.size()];

        for(int i = 0; i < chips.length; i++) {
            chips[i] = (JSONObject) chipsArr.get(i);
        }

        for(JSONObject chip: chips) {
            if(!chip.get("type").equals("AND") && !chip.get("type").equals("OR") && !chip.get("type").equals("NOT")) {
                if(!isLoaded((String) chip.get("name"))) {
                    loadFromJson((String) chip.get("name"));
                }
                getChip(name).addChip(getChip((String) chip.get("name")));
            } else {
                Chip.Defaults defaultMode;
                switch((String) chip.get("type")) {
                    case "AND":
                        defaultMode = Chip.Defaults.AND;
                        break;

                    case "OR":
                        defaultMode = Chip.Defaults.OR;
                        break;

                    case "NOT":
                        defaultMode = Chip.Defaults.NOT;
                        break;

                    default:
                        defaultMode = Chip.Defaults.NONE;
                        break;
                }
                getChip(name).addChip(new Chip((String) chip.get("name"), defaultMode));
            }
        }

        JSONArray connectionsArr = (JSONArray) json.get("connections");
        JSONObject[] connections = new JSONObject[connectionsArr.size()];

        for(int i = 0; i < connections.length; i++) {
            connections[i] = (JSONObject) connectionsArr.get(i);
        }

        for(JSONObject connection: connections) {
            String from = (String) connection.get("from");
            String to = (String) connection.get("to");

            String[] fromParts = from.split(":");
            String[] toParts = to.split(":");

            if(fromParts[0].equals(name)) {
                getChip(name).connectIn(fromParts[1], toParts[0], toParts[1]);
            } else if(toParts[0].equals(name)) {
                getChip(name).connectOut(fromParts[0], fromParts[1], toParts[1]);
            } else {
                getChip(name).interconnect(fromParts[0], fromParts[1], toParts[0], toParts[1]);
            }
        }

        try {
            assert reader != null;
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return getChip(name);
    }

    public boolean isLoaded(String name) {
        for(Chip p: chipList) {
            if(p.name.equals(name)) {
                return true;
            }
        }
        return false;
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

    public void printTruthTable(Chip c) {
        int totalCombinations = 1 << c.iPins.size(); // 2^n combinations
        boolean[][] truthTable = new boolean[totalCombinations][c.iPins.size() + c.oPins.size()];

        for (int i = 0; i < totalCombinations; i++) {
            for (int j = 0; j < c.iPins.size(); j++) {
                truthTable[i][j] = (i & (1 << j)) != 0; // Extract bit as boolean
            }
        }

        for(int i = 0; i < truthTable.length; i++) {
            for(int j = 0; j < c.iPins.size(); j++) {
                c.iPins.get(j).activated = truthTable[i][j];
            }
            runSimulation(c);
            for(int j = 0; j < c.oPins.size(); j++) {
                truthTable[i][c.iPins.size() + j] = c.oPins.get(j).activated;
            }
        }

        String[][] truthTableString = new String[totalCombinations][c.iPins.size() + c.oPins.size()];

        for(int i = 0; i < truthTable.length;i++) {
            for(int j = 0; j < truthTable[i].length; j++) {
                truthTableString[i][j] = Boolean.toString(truthTable[i][j]);
            }
        }

        ASCIITableHeader[] header = new ASCIITableHeader[c.iPins.size() + c.oPins.size()];

        ArrayList<Pin> both = new ArrayList<>();
        both.addAll(c.iPins);
        both.addAll(c.oPins);

        for(int i = 0; i < both.size(); i++) {
            header[i] = new ASCIITableHeader(both.get(i).name, ASCIITable.ALIGN_LEFT);
        }

        ASCIITable.getInstance().printTable(header, truthTableString);
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
