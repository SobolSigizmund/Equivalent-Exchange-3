package com.pahimar.ee3.exchange;

import com.pahimar.ee3.api.exchange.EnergyValue;
import com.pahimar.ee3.util.LogHelper;
import com.pahimar.ee3.util.SerializationHelper;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

public class NewEnergyValueRegistry {

    public static final Marker ENERGY_VALUE_MARKER = MarkerManager.getMarker("EE3_ENERGY_VALUE", LogHelper.MOD_MARKER);
    public static final NewEnergyValueRegistry INSTANCE = new NewEnergyValueRegistry();

    public final Map<WrappedStack, EnergyValue> preCalculationMappings;
    public final Map<WrappedStack, EnergyValue> postCalculationMappings;

    public static File energyValuesDataDirectory;
    public static File energyValuesDataFile;

    private NewEnergyValueRegistry() {

        preCalculationMappings = new TreeMap<>();

        // Loading up some dummy values for testing serialization
        preCalculationMappings.put(WrappedStack.wrap(Items.apple), new EnergyValue(1));
        preCalculationMappings.put(WrappedStack.wrap(Items.arrow), new EnergyValue(2));
        preCalculationMappings.put(WrappedStack.wrap(Items.baked_potato), new EnergyValue(3));
        preCalculationMappings.put(WrappedStack.wrap(Items.bed), new EnergyValue(4));
        preCalculationMappings.put(WrappedStack.wrap(new OreStack("oreIron")), new EnergyValue(5));

        postCalculationMappings = new TreeMap<>();
    }

    public String toJson() {
        return SerializationHelper.GSON.toJson(this);
    }

    /**
     *  @see net.minecraft.nbt.CompressedStreamTools#safeWrite(NBTTagCompound, File)
     */
    public void save() throws IOException {

        File file = energyValuesDataFile;
        File tempFile = new File(file.getAbsolutePath() + "_tmp");

        if (tempFile.exists()) {
            tempFile.delete();
        }

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
        bufferedWriter.write(this.toJson());
        bufferedWriter.close();

        if (file.exists()) {
            file.delete();
        }

        if (file.exists()) {
            throw new IOException("Failed to delete " + file);
        }
        else {
            tempFile.renameTo(file);
        }
    }

    public void load() {

        File file = energyValuesDataFile;

        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {

            stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String jsonRegistryString = stringBuilder.toString();
        LogHelper.info(jsonRegistryString);

        NewEnergyValueRegistry newEnergyValueRegistry = SerializationHelper.GSON.fromJson(jsonRegistryString, NewEnergyValueRegistry.class);

        LogHelper.info(newEnergyValueRegistry);
    }
}
