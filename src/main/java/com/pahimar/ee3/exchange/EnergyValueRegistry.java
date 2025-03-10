package com.pahimar.ee3.exchange;

import com.google.common.collect.ImmutableSortedMap;
import com.google.gson.*;
import com.pahimar.ee3.api.exchange.EnergyValue;
import com.pahimar.ee3.api.exchange.EnergyValueRegistryProxy;
import com.pahimar.ee3.api.exchange.IEnergyValueProvider;
import com.pahimar.ee3.recipe.RecipeRegistry;
import com.pahimar.ee3.reference.Files;
import com.pahimar.ee3.reference.Reference;
import com.pahimar.ee3.reference.Settings;
import com.pahimar.ee3.util.EnergyValueHelper;
import com.pahimar.ee3.util.LoaderHelper;
import com.pahimar.ee3.util.LogHelper;
import com.pahimar.ee3.util.SerializationHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;

public class EnergyValueRegistry implements JsonSerializer<EnergyValueRegistry>, JsonDeserializer<EnergyValueRegistry>
{
    public static final Marker ENERGY_VALUE_MARKER = MarkerManager.getMarker("EE3_ENERGY_VALUE", LogHelper.MOD_MARKER);
    private static final Marker PRE_CALC_MARKER = MarkerManager.getMarker("EE3_ENERGY_VALUE_PRE_CALC", ENERGY_VALUE_MARKER);
    private static final Marker POST_CALC_MARKER = MarkerManager.getMarker("EE3_ENERGY_VALUE_POST_CALC", ENERGY_VALUE_MARKER);

    private static final Gson JSON_SERIALIZER = (new GsonBuilder()).setPrettyPrinting().registerTypeAdapter(EnergyValueRegistry.class, new EnergyValueRegistry()).registerTypeAdapter(EnergyValueStackMapping.class, new EnergyValueStackMapping()).create();
    private static EnergyValueRegistry energyValueRegistry = null;
    private static Map<WrappedStack, EnergyValue> preCalculationMappings;
    private static Map<WrappedStack, EnergyValue> postCalculationMappings;
    private boolean shouldRegenNextRestart = false;
    private ImmutableSortedMap<WrappedStack, EnergyValue> stackMappings;
    private ImmutableSortedMap<EnergyValue, List<WrappedStack>> valueMappings;
    private SortedSet<WrappedStack> uncomputedStacks;

    private EnergyValueRegistry()
    {
    }

    public static EnergyValueRegistry getInstance()
    {
        if (energyValueRegistry == null)
        {
            energyValueRegistry = new EnergyValueRegistry();
        }


        return energyValueRegistry;
    }

    public void addPreCalculationEnergyValue(Object object, float energyValue)
    {
        addPreCalculationEnergyValue(object, new EnergyValue(energyValue));
    }

    public void addPreCalculationEnergyValue(Object object, EnergyValue energyValue)
    {
        if (preCalculationMappings == null)
        {
            preCalculationMappings = new TreeMap<WrappedStack, EnergyValue>();
        }

        if (WrappedStack.canBeWrapped(object) && energyValue != null && Float.compare(energyValue.getValue(), 0f) > 0)
        {
            WrappedStack wrappedStack = WrappedStack.wrap(object);

            if (wrappedStack.getStackSize() > 0)
            {
                WrappedStack factoredWrappedStack = WrappedStack.wrap(wrappedStack, 1);
                EnergyValue factoredEnergyValue = EnergyValueHelper.factorEnergyValue(energyValue, wrappedStack.getStackSize());

                if (preCalculationMappings.containsKey(factoredWrappedStack))
                {
                    if (factoredEnergyValue.compareTo(preCalculationMappings.get(factoredWrappedStack)) < 0)
                    {
                        LogHelper.trace(PRE_CALC_MARKER, "[{}] Mod with ID '{}' set a pre-calculation energy value of {} for object {}", LoaderHelper.getLoaderState(), Loader.instance().activeModContainer().getModId(), energyValue, wrappedStack);
                        preCalculationMappings.put(factoredWrappedStack, factoredEnergyValue);
                    }
                }
                else
                {
                    LogHelper.trace(PRE_CALC_MARKER, "[{}] Mod with ID '{}' set a pre-calculation energy value of {} for object {}", LoaderHelper.getLoaderState(), Loader.instance().activeModContainer().getModId(), energyValue, wrappedStack);
                    preCalculationMappings.put(factoredWrappedStack, factoredEnergyValue);
                }
            }
        }
    }

    public void addPostCalculationExactEnergyValue(Object object, float energyValue)
    {
        addPostCalculationExactEnergyValue(object, new EnergyValue(energyValue));
    }

    public void addPostCalculationExactEnergyValue(Object object, EnergyValue energyValue)
    {
        if (postCalculationMappings == null)
        {
            postCalculationMappings = new TreeMap<WrappedStack, EnergyValue>();
        }

        if (WrappedStack.canBeWrapped(object) && energyValue != null && Float.compare(energyValue.getValue(), 0f) > 0)
        {
            WrappedStack wrappedStack = WrappedStack.wrap(object);

            if (wrappedStack.getStackSize() > 0)
            {
                WrappedStack factoredWrappedStack = WrappedStack.wrap(wrappedStack, 1);
                EnergyValue factoredEnergyValue = EnergyValueHelper.factorEnergyValue(energyValue, wrappedStack.getStackSize());

                LogHelper.trace(POST_CALC_MARKER, "[{}] Mod with ID '{}' set a post-calculation energy value of {} for object {}", LoaderHelper.getLoaderState(), Loader.instance().activeModContainer().getModId(), energyValue, wrappedStack);
                postCalculationMappings.put(factoredWrappedStack, factoredEnergyValue);
            }
        }
    }

    public boolean hasEnergyValue(Object object)
    {
        return hasEnergyValue(object, false);
    }

    public boolean hasEnergyValue(Object object, boolean strict)
    {
        return getEnergyValue(object, strict) != null;
    }

    public EnergyValue getEnergyValue(Object object)
    {
        return getEnergyValue(EnergyValueRegistryProxy.Phase.ALL, object, false);
    }

    public EnergyValue getEnergyValue(Object object, boolean strict)
    {
        return getEnergyValue(EnergyValueRegistryProxy.Phase.ALL, object, strict);
    }

    public EnergyValue getEnergyValue(EnergyValueRegistryProxy.Phase phase, Object object, boolean strict)
    {
        if (phase == EnergyValueRegistryProxy.Phase.PRE_ASSIGNMENT || phase == EnergyValueRegistryProxy.Phase.PRE_CALCULATION)
        {
            return getEnergyValueFromMap(preCalculationMappings, object, strict);
        }
        else if (phase == EnergyValueRegistryProxy.Phase.POST_ASSIGNMENT || phase == EnergyValueRegistryProxy.Phase.POST_CALCULATION)
        {
            return getEnergyValueFromMap(postCalculationMappings, object, strict);
        }
        else
        {
            return getEnergyValueFromMap(energyValueRegistry.stackMappings, object, strict);
        }
    }

    public EnergyValue getEnergyValueForStack(Object object, boolean strict)
    {
        WrappedStack wrappedObject = WrappedStack.wrap(object);

        if (wrappedObject != null && getEnergyValue(object, strict) != null)
        {
            return new EnergyValue(getEnergyValue(object, strict).getValue() * wrappedObject.getStackSize());
        }

        return null;
    }

    public EnergyValue getEnergyValueFromMap(Map<WrappedStack, EnergyValue> stackEnergyValueMap, Object object)
    {
        return getEnergyValueFromMap(stackEnergyValueMap, object, false);
    }

    public EnergyValue getEnergyValueFromMap(Map<WrappedStack, EnergyValue> stackEnergyValueMap, Object object, boolean strict)
    {
        if (WrappedStack.canBeWrapped(object))
        {
            WrappedStack wrappedStackObject = WrappedStack.wrap(object);
            WrappedStack unitWrappedStackObject = WrappedStack.wrap(object);
            unitWrappedStackObject.setStackSize(1);
            Object wrappedObject = wrappedStackObject.getWrappedObject();

            /**
             *  In the event that an Item has an IEnergyValueProvider implementation, route the call to the implementation
             */
            if (wrappedObject instanceof ItemStack && ((ItemStack) wrappedObject).getItem() instanceof IEnergyValueProvider && !strict)
            {
                ItemStack itemStack = (ItemStack) wrappedObject;
                IEnergyValueProvider iEnergyValueProvider = (IEnergyValueProvider) itemStack.getItem();
                EnergyValue energyValue = iEnergyValueProvider.getEnergyValue(itemStack);

                if (energyValue != null && energyValue.getValue() > 0f)
                {
                    return energyValue;
                }
            }
            else if (stackEnergyValueMap != null)
            {
                /**
                 *  Check for a direct value mapping for the object
                 */
                if (stackEnergyValueMap.containsKey(unitWrappedStackObject))
                {
                    return stackEnergyValueMap.get(unitWrappedStackObject);
                }
                else if (!strict)
                {
                    if (wrappedObject instanceof ItemStack)
                    {
                        EnergyValue lowestValue = null;
                        ItemStack wrappedItemStack = (ItemStack) wrappedObject;

                        /**
                         *  The ItemStack does not have a direct mapping, so check if it is a member of an OreDictionary
                         *  entry. If it is a member of an OreDictionary entry, check if every ore name it is associated
                         *  with has 1) a direct mapping, and 2) the same mapping value
                         */
                        if (OreDictionary.getOreIDs(wrappedItemStack).length >= 1)
                        {
                            EnergyValue energyValue = null;
                            boolean allHaveSameValueFlag = true;

                            // Scan all valid ore dictionary values, if they ALL have the same value, then return it
                            for (int oreID : OreDictionary.getOreIDs(wrappedItemStack))
                            {
                                String oreName = OreDictionary.getOreName(oreID);
                                if (!oreName.equals("Unknown"))
                                {
                                    WrappedStack oreStack = WrappedStack.wrap(new OreStack(oreName));

                                    if (oreStack != null && stackEnergyValueMap.containsKey(oreStack))
                                    {
                                        if (energyValue == null)
                                        {
                                            energyValue = stackEnergyValueMap.get(oreStack);
                                        }
                                        else if (!energyValue.equals(stackEnergyValueMap.get(oreStack)))
                                        {
                                            allHaveSameValueFlag = false;
                                        }
                                    }
                                    else
                                    {
                                        allHaveSameValueFlag = false;
                                    }
                                }
                                else
                                {
                                    allHaveSameValueFlag = false;
                                }
                            }

                            if (energyValue != null && allHaveSameValueFlag)
                            {
                                return energyValue;
                            }
                        }
                        else
                        {
                            /**
                             *  Scan the stack value map for ItemStacks that have the same Item. If one is found, check
                             *  if it has a wildcard meta value (and therefore is considered the same). Otherwise, check
                             *  if the ItemStack is "damageable" and calculate the value for the damaged stack.
                             */
                            for (WrappedStack valuedStack : stackEnergyValueMap.keySet())
                            {
                                if (valuedStack.getWrappedObject() instanceof ItemStack)
                                {
                                    ItemStack valuedItemStack = (ItemStack) valuedStack.getWrappedObject();

                                    if (Item.getIdFromItem(valuedItemStack.getItem()) == Item.getIdFromItem(wrappedItemStack.getItem()))
                                    {
                                        if (valuedItemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE || wrappedItemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
                                        {
                                            EnergyValue stackValue = stackEnergyValueMap.get(valuedStack);

                                            if (stackValue.compareTo(lowestValue) < 0)
                                            {
                                                lowestValue = stackValue;
                                            }
                                        }
                                        else if (wrappedItemStack.getItem().isDamageable() && wrappedItemStack.isItemDamaged())
                                        {
                                            EnergyValue stackValue = new EnergyValue(stackEnergyValueMap.get(valuedStack).getValue() * (1 - (wrappedItemStack.getItemDamage() * 1.0F / wrappedItemStack.getMaxDamage())));

                                            if (stackValue.compareTo(lowestValue) < 0)
                                            {
                                                lowestValue = stackValue;
                                            }
                                        }
                                    }
                                }
                            }

                            return lowestValue;
                        }
                    }
                    else if (wrappedObject instanceof OreStack)
                    {
                        OreStack oreStack = (OreStack) wrappedObject;

                        if (CachedOreDictionary.getInstance().getItemStacksForOreName(oreStack.oreName).size() >= 1)
                        {
                            EnergyValue energyValue = null;
                            boolean allHaveSameValueFlag = true;

                            // Scan all valid ore dictionary values, if they ALL have the same value, then return it
                            for (ItemStack itemStack : CachedOreDictionary.getInstance().getItemStacksForOreName(oreStack.oreName))
                            {
                                WrappedStack wrappedItemStack = WrappedStack.wrap(itemStack);

                                if (wrappedItemStack != null && stackEnergyValueMap.containsKey(wrappedItemStack))
                                {
                                    if (energyValue == null)
                                    {
                                        energyValue = stackEnergyValueMap.get(wrappedItemStack);
                                    }
                                    else if (!energyValue.equals(stackEnergyValueMap.get(wrappedItemStack)))
                                    {
                                        allHaveSameValueFlag = false;
                                    }
                                }
                                else
                                {
                                    allHaveSameValueFlag = false;
                                }
                            }

                            if (energyValue != null && allHaveSameValueFlag)
                            {
                                return energyValue;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    protected final void init()
    {
        if (!loadEnergyValueRegistryFromFile())
        {
            runDynamicEnergyValueResolution();
        }
        this.shouldRegenNextRestart = false;
    }

    private void runDynamicEnergyValueResolution()
    {
        TreeMap<WrappedStack, EnergyValue> stackValueMap = new TreeMap<WrappedStack, EnergyValue>();
        uncomputedStacks = null;

        // Add in all mod specified pre-calculation values
        stackValueMap.putAll(preCalculationMappings); // TODO Logging

        // Add in all global pre-calculation values
        LogHelper.trace(ENERGY_VALUE_MARKER, "Adding EnergyValue mappings from {}", Files.Global.preCalcluationEnergyValueFile);
        Map<WrappedStack, EnergyValue> globalPreCalculationValueMap = SerializationHelper.readEnergyValueStackMapFromJsonFile(Files.Global.preCalcluationEnergyValueFile);
        for (WrappedStack wrappedStack : globalPreCalculationValueMap.keySet())
        {
            if (globalPreCalculationValueMap.get(wrappedStack) != null)
            {
                stackValueMap.put(wrappedStack, globalPreCalculationValueMap.get(wrappedStack));
                LogHelper.trace(ENERGY_VALUE_MARKER, "Adding EnergyValue {} for {}", globalPreCalculationValueMap.get(wrappedStack), wrappedStack);
            }
        }

        // Add in all instance pre-calculation values
        LogHelper.trace(ENERGY_VALUE_MARKER, "Adding EnergyValue mappings from {}", Files.PRE_CALCULATION_ENERGY_VALUES);
        Map<WrappedStack, EnergyValue> instancePreAssignedValueMap = SerializationHelper.readEnergyValueStackMapFromJsonFile(Files.PRE_CALCULATION_ENERGY_VALUES);
        for (WrappedStack wrappedStack : instancePreAssignedValueMap.keySet())
        {
            if (instancePreAssignedValueMap.get(wrappedStack) != null)
            {
                stackValueMap.put(wrappedStack, instancePreAssignedValueMap.get(wrappedStack));
                LogHelper.trace(ENERGY_VALUE_MARKER, "Adding EnergyValue {} for {}", instancePreAssignedValueMap.get(wrappedStack), wrappedStack);
            }
        }

        /*
         *  Auto-assignment
         */
        Map<WrappedStack, EnergyValue> computedStackValues;
        int passNumber = 0;
        long computationStartTime = System.currentTimeMillis();
        long passStartTime;
        int passComputedValueCount = 0;
        int totalComputedValueCount = 0;
        LogHelper.info(ENERGY_VALUE_MARKER, "Beginning dynamic value calculation");
        boolean isFirstPass = true;
        while ((isFirstPass || passComputedValueCount > 0) && (passNumber < 16))
        {
            if (isFirstPass)
            {
                isFirstPass = false;
            }
            passComputedValueCount = 0;
            passStartTime = System.currentTimeMillis();
            passNumber++;

            // Compute stack mappings from existing stack mappings
            computedStackValues = computeStackMappings(stackValueMap, passNumber);

            for (WrappedStack keyStack : computedStackValues.keySet())
            {
                EnergyValue factoredExchangeEnergyValue = null;
                WrappedStack factoredKeyStack = null;

                if (keyStack != null && keyStack.getWrappedObject() != null && keyStack.getStackSize() > 0)
                {
                    if (computedStackValues.get(keyStack) != null && Float.compare(computedStackValues.get(keyStack).getValue(), 0f) > 0)
                    {
                        factoredExchangeEnergyValue = EnergyValueHelper.factorEnergyValue(computedStackValues.get(keyStack), keyStack.getStackSize());
                        factoredKeyStack = WrappedStack.wrap(keyStack, 1);
                    }
                }

                if (factoredExchangeEnergyValue != null)
                {
                    if (stackValueMap.containsKey(factoredKeyStack))
                    {
                        if (factoredExchangeEnergyValue.compareTo(stackValueMap.get(factoredKeyStack)) == -1)
                        {
//                            LogHelper.trace(String.format(""));  TODO Log message
                            stackValueMap.put(factoredKeyStack, factoredExchangeEnergyValue);
                        }
                    }
                    else
                    {
//                        LogHelper.trace(String.format(""));  TODO Log message
                        stackValueMap.put(factoredKeyStack, factoredExchangeEnergyValue);
                        passComputedValueCount++;
                        totalComputedValueCount++;
                    }
                }
            }
            LogHelper.info(ENERGY_VALUE_MARKER, "Pass {}: Calculated {} values for objects in {} ms", passNumber, passComputedValueCount, System.currentTimeMillis() - passStartTime);
        }
        LogHelper.info(ENERGY_VALUE_MARKER, "Finished dynamic value calculation (calculated {} values for objects in {} ms)", totalComputedValueCount, System.currentTimeMillis() - computationStartTime);

        // Add in all mod specified post-calculation values
        // TODO Logging
        if (postCalculationMappings != null)
        {
            for (WrappedStack wrappedStack : postCalculationMappings.keySet())
            {
                if (postCalculationMappings.get(wrappedStack) != null)
                {
                    stackValueMap.put(wrappedStack, postCalculationMappings.get(wrappedStack));
                }
            }
        }
        else
        {
            postCalculationMappings = new TreeMap<WrappedStack, EnergyValue>();
        }

        // Add in all global post-calculation values
        LogHelper.trace(ENERGY_VALUE_MARKER, "Adding EnergyValue mappings from {}", Files.Global.postCalcluationEnergyValueFile);
        Map<WrappedStack, EnergyValue> globalPostCalculationValueMap = SerializationHelper.readEnergyValueStackMapFromJsonFile(Files.Global.postCalcluationEnergyValueFile);
        for (WrappedStack wrappedStack : globalPostCalculationValueMap.keySet())
        {
            if (globalPostCalculationValueMap.get(wrappedStack) != null)
            {
                stackValueMap.put(wrappedStack, globalPostCalculationValueMap.get(wrappedStack));
                LogHelper.trace(ENERGY_VALUE_MARKER, "Adding EnergyValue {} for {}", globalPostCalculationValueMap.get(wrappedStack), wrappedStack);
            }
        }

        // Add in all instance post-calculation values
        LogHelper.trace(ENERGY_VALUE_MARKER, "Adding EnergyValue mappings from {}", Files.POST_CALCULATION_ENERGY_VALUES);
        Map<WrappedStack, EnergyValue> instancePostCalculationValueMap = SerializationHelper.readEnergyValueStackMapFromJsonFile(Files.POST_CALCULATION_ENERGY_VALUES);
        for (WrappedStack wrappedStack : instancePostCalculationValueMap.keySet())
        {
            if (instancePostCalculationValueMap.get(wrappedStack) != null)
            {
                stackValueMap.put(wrappedStack, instancePostCalculationValueMap.get(wrappedStack));
                LogHelper.trace(ENERGY_VALUE_MARKER, "Adding EnergyValue {} for {}", instancePreAssignedValueMap.get(wrappedStack), wrappedStack);
            }
        }

        /**
         * Finalize the stack to value map
         */
        ImmutableSortedMap.Builder<WrappedStack, EnergyValue> stackMappingsBuilder = ImmutableSortedMap.naturalOrder();
        stackMappingsBuilder.putAll(stackValueMap);
        stackMappings = stackMappingsBuilder.build();

        /**
         *  Value map resolution
         */
        generateValueStackMappings();

        // Serialize values to disk
        LogHelper.info(ENERGY_VALUE_MARKER, "Saving energy values to disk");
        save();

        // TODO Make this make "sense" and also ensure it's added as an option to the debug command
        for (WrappedStack wrappedStack : uncomputedStacks)
        {
            if (!hasEnergyValue(wrappedStack))
            {
                LogHelper.info(ENERGY_VALUE_MARKER, "Unable to compute a value for object '{}'", wrappedStack);
            }
        }
    }

    private void generateValueStackMappings()
    {
        SortedMap<EnergyValue, List<WrappedStack>> tempValueMappings = new TreeMap<EnergyValue, List<WrappedStack>>();

        for (WrappedStack stack : stackMappings.keySet())
        {
            if (stack != null)
            {
                EnergyValue value = stackMappings.get(stack);

                if (value != null)
                {
                    if (tempValueMappings.containsKey(value))
                    {
                        if (!(tempValueMappings.get(value).contains(stack)))
                        {
                            tempValueMappings.get(value).add(stack);
                        }
                    }
                    else
                    {
                        tempValueMappings.put(value, new ArrayList<WrappedStack>(Arrays.asList(stack)));
                    }
                }
            }
        }
        valueMappings = ImmutableSortedMap.copyOf(tempValueMappings);
    }

    private Map<WrappedStack, EnergyValue> computeStackMappings(Map<WrappedStack, EnergyValue> stackValueMappings, int passCount)
    {
        Map<WrappedStack, EnergyValue> computedStackMap = new TreeMap<WrappedStack, EnergyValue>();

        for (WrappedStack recipeOutput : RecipeRegistry.getInstance().getRecipeMappings().keySet())
        {
            // TODO Review: possible fault in the logic here that is preventing some values from being assigned?
            if (!hasEnergyValue(recipeOutput.getWrappedObject(), false) && !computedStackMap.containsKey(recipeOutput))
            {
                EnergyValue lowestValue = null;

                for (List<WrappedStack> recipeInputs : RecipeRegistry.getInstance().getRecipeMappings().get(recipeOutput))
                {
                    EnergyValue computedValue = EnergyValueHelper.computeEnergyValueFromRecipe(stackValueMappings, recipeOutput, recipeInputs);

                    if (computedValue != null)
                    {
                        if (computedValue.compareTo(lowestValue) < 0)
                        {
                            lowestValue = computedValue;
                        }
                    }
                    else
                    {
                        if (uncomputedStacks == null)
                        {
                            uncomputedStacks = new TreeSet<WrappedStack>();
                        }

                        uncomputedStacks.add(recipeOutput);
                    }
                }

                if ((lowestValue != null) && (lowestValue.getValue() > 0f))
                {
                    computedStackMap.put(WrappedStack.wrap(recipeOutput.getWrappedObject()), lowestValue);
                }
            }
        }

        return computedStackMap;
    }

    public List getStacksInRange(int start, int finish)
    {
        return getStacksInRange(new EnergyValue(start), new EnergyValue(finish));
    }

    public List getStacksInRange(float start, float finish)
    {
        return getStacksInRange(new EnergyValue(start), new EnergyValue(finish));
    }

    public List getStacksInRange(EnergyValue start, EnergyValue finish)
    {
        List stacksInRange = new ArrayList<WrappedStack>();

        if (valueMappings != null)
        {
            SortedMap<EnergyValue, List<WrappedStack>> tailMap = energyValueRegistry.valueMappings.tailMap(start);
            SortedMap<EnergyValue, List<WrappedStack>> headMap = energyValueRegistry.valueMappings.headMap(finish);

            SortedMap<EnergyValue, List<WrappedStack>> smallerMap;
            SortedMap<EnergyValue, List<WrappedStack>> biggerMap;

            if (!tailMap.isEmpty() && !headMap.isEmpty())
            {

                if (tailMap.size() <= headMap.size())
                {
                    smallerMap = tailMap;
                    biggerMap = headMap;
                }
                else
                {
                    smallerMap = headMap;
                    biggerMap = tailMap;
                }

                for (EnergyValue value : smallerMap.keySet())
                {
                    if (biggerMap.containsKey(value))
                    {
                        for (WrappedStack wrappedStack : energyValueRegistry.valueMappings.get(value))
                        {
                            if (wrappedStack.getWrappedObject() instanceof ItemStack || wrappedStack.getWrappedObject() instanceof FluidStack)
                            {
                                stacksInRange.add(wrappedStack.getWrappedObject());
                            }
                            else if (wrappedStack.getWrappedObject() instanceof OreStack)
                            {
                                for (ItemStack itemStack : OreDictionary.getOres(((OreStack) wrappedStack.getWrappedObject()).oreName))
                                {
                                    stacksInRange.add(itemStack);
                                }
                            }
                        }
                    }
                }
            }
        }

        return stacksInRange;
    }

    public void loadFromMap(Map<WrappedStack, EnergyValue> stackValueMap) {
        if (stackValueMap != null) {
            ImmutableSortedMap.Builder<WrappedStack, EnergyValue> stackMappingsBuilder = ImmutableSortedMap.naturalOrder();
            stackMappingsBuilder.putAll(stackValueMap);
            stackMappings = stackMappingsBuilder.build();

            /**
             *  Resolve value stack mappings from the newly loaded stack mappings
             */
            generateValueStackMappings();
        }
    }

    public void setEnergyValue(WrappedStack wrappedStack, EnergyValue energyValue) {
        if (wrappedStack != null && energyValue != null && Float.compare(energyValue.getValue(), 0f) > 0) {
            TreeMap<WrappedStack, EnergyValue> stackValueMap = new TreeMap<WrappedStack, EnergyValue>(stackMappings);
            stackValueMap.put(wrappedStack, energyValue);

            ImmutableSortedMap.Builder<WrappedStack, EnergyValue> stackMappingsBuilder = ImmutableSortedMap.naturalOrder();
            stackMappingsBuilder.putAll(stackValueMap);
            stackMappings = stackMappingsBuilder.build();

            generateValueStackMappings();
        }
    }

    public boolean getShouldRegenNextRestart() {
        return shouldRegenNextRestart;
    }

    public void setShouldRegenNextRestart(boolean shouldRegenNextRestart) {
        this.shouldRegenNextRestart = shouldRegenNextRestart;
    }

    public ImmutableSortedMap<WrappedStack, EnergyValue> getStackValueMap() {
        return stackMappings;
    }

    public ImmutableSortedMap<EnergyValue, List<WrappedStack>> getValueStackMap() {
        return valueMappings;
    }

    public void save() {

        File energyValuesDataDirectory = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getSaveHandler().getWorldDirectory(), "data" + File.separator + Reference.LOWERCASE_MOD_ID + File.separator + "energyvalues");
        energyValuesDataDirectory.mkdirs();

        if (shouldRegenNextRestart) {
            File staticEnergyValuesJsonFile = new File(energyValuesDataDirectory, Files.ENERGY_VALUES_JSON);
            File md5EnergyValuesJsonFile = new File(energyValuesDataDirectory, SerializationHelper.getModListMD5() + ".json");

            // JSON
            if (staticEnergyValuesJsonFile.exists()) {
                staticEnergyValuesJsonFile.delete();
            }
            if (md5EnergyValuesJsonFile.exists()) {
                md5EnergyValuesJsonFile.delete();
            }

            shouldRegenNextRestart = false;
        } else {
            SerializationHelper.compressEnergyValueStackMapToFile(new File(energyValuesDataDirectory, Files.ENERGY_VALUES_JSON), energyValueRegistry.stackMappings);
            SerializationHelper.compressEnergyValueStackMapToFile(new File(energyValuesDataDirectory, SerializationHelper.getModListMD5() + ".json.gz"), energyValueRegistry.stackMappings);
        }
    }

    public boolean loadFromFile(File energyValueFile) {
        if (energyValueFile != null) {
            LogHelper.info(ENERGY_VALUE_MARKER, "Attempting to load energy values from file: {}", energyValueFile.getAbsolutePath());
        }

        return false;
    }

    public boolean loadEnergyValueRegistryFromFile() {

        File energyValuesDataDirectory = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getSaveHandler().getWorldDirectory(), "data" + File.separator + Reference.LOWERCASE_MOD_ID + File.separator + "energyvalues");
        energyValuesDataDirectory.mkdirs();

        File staticEnergyValuesFile = new File(energyValuesDataDirectory, Files.ENERGY_VALUES_JSON);
        File md5EnergyValuesFile = new File(energyValuesDataDirectory, SerializationHelper.getModListMD5() + ".json.gz");

        Map<WrappedStack, EnergyValue> stackValueMap = null;

        loadFromFile(new File(Files.Global.dataDirectory, Files.ENERGY_VALUES_JSON));

        if (!Settings.DynamicEnergyValueGeneration.regenerateEnergyValuesWhen.equalsIgnoreCase("Always")) {
            if (Settings.DynamicEnergyValueGeneration.regenerateEnergyValuesWhen.equalsIgnoreCase("When Mods Change")) {
                if (md5EnergyValuesFile.exists()) {
                    LogHelper.info(ENERGY_VALUE_MARKER, "Attempting to load energy values from file: {}", md5EnergyValuesFile.getAbsolutePath());
                    stackValueMap = SerializationHelper.decompressEnergyValueStackMapFromFile(md5EnergyValuesFile);
                }
            } else if (Settings.DynamicEnergyValueGeneration.regenerateEnergyValuesWhen.equalsIgnoreCase("Never")) {
                if (staticEnergyValuesFile.exists()) {
                    LogHelper.info(ENERGY_VALUE_MARKER, "Attempting to load energy values from file: {}", staticEnergyValuesFile.getAbsolutePath());
                    stackValueMap = SerializationHelper.decompressEnergyValueStackMapFromFile(staticEnergyValuesFile);
                } else if (md5EnergyValuesFile.exists()) {
                    LogHelper.info(ENERGY_VALUE_MARKER, "Attempting to load energy values from file: {}", md5EnergyValuesFile.getAbsolutePath());
                    stackValueMap = SerializationHelper.decompressEnergyValueStackMapFromFile(md5EnergyValuesFile);
                }
            }

            if (stackValueMap != null) {
                loadFromMap(stackValueMap);
                LogHelper.info(ENERGY_VALUE_MARKER, "Successfully loaded energy values from file");
                return true;
            } else {
                LogHelper.info(ENERGY_VALUE_MARKER, "No energy value file to load values from, generating new values");
                return false;
            }
        } else {
            return false;
        }
    }

    public String toJson()
    {
        return JSON_SERIALIZER.toJson(this);
    }

    @Override
    public EnergyValueRegistry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        if (json.isJsonArray()) {
            JsonArray jsonArray = (JsonArray) json;
            Map<WrappedStack, EnergyValue> stackValueMap = new TreeMap<WrappedStack, EnergyValue>();
            Iterator<JsonElement> iterator = jsonArray.iterator();

            while (iterator.hasNext()) {
                JsonElement jsonElement = iterator.next();
                EnergyValueStackMapping energyValueStackMapping = new EnergyValueStackMapping().deserialize(jsonElement, typeOfT, context);

                if (energyValueStackMapping != null) {
                    stackValueMap.put(energyValueStackMapping.wrappedStack, energyValueStackMapping.energyValue);
                }
            }

            ImmutableSortedMap.Builder<WrappedStack, EnergyValue> stackMappingsBuilder = ImmutableSortedMap.naturalOrder();
            stackMappingsBuilder.putAll(stackValueMap);
            stackMappings = stackMappingsBuilder.build();

            generateValueStackMappings();
        }

        return null;
    }

    @Override
    public JsonElement serialize(EnergyValueRegistry energyValueRegistry, Type typeOfSrc, JsonSerializationContext context) {

        JsonArray jsonEnergyValueRegistry = new JsonArray();

        for (WrappedStack wrappedStack : energyValueRegistry.stackMappings.keySet())
        {
            jsonEnergyValueRegistry.add(EnergyValueStackMapping.jsonSerializer.toJsonTree(new EnergyValueStackMapping(wrappedStack, energyValueRegistry.stackMappings.get(wrappedStack))));
        }

        return jsonEnergyValueRegistry;
    }

    public void dumpEnergyValueRegistryToLog() {

        dumpEnergyValueRegistryToLog(EnergyValueRegistryProxy.Phase.ALL);
    }

    public void dumpEnergyValueRegistryToLog(EnergyValueRegistryProxy.Phase phase) {

        LogHelper.info(ENERGY_VALUE_MARKER, "BEGIN DUMPING {} ENERGY VALUE MAPPINGS", phase);
        if (phase == EnergyValueRegistryProxy.Phase.PRE_ASSIGNMENT || phase == EnergyValueRegistryProxy.Phase.PRE_CALCULATION) {
            for (WrappedStack wrappedStack : this.preCalculationMappings.keySet()) {
                LogHelper.info(ENERGY_VALUE_MARKER, "Object: {}, Value: {}", wrappedStack, EnergyValueRegistry.getInstance().getStackValueMap().get(wrappedStack));
            }
        } else if (phase == EnergyValueRegistryProxy.Phase.POST_ASSIGNMENT || phase == EnergyValueRegistryProxy.Phase.POST_CALCULATION) {
            if (this.postCalculationMappings != null) {
                for (WrappedStack wrappedStack : this.postCalculationMappings.keySet()) {
                    LogHelper.info(ENERGY_VALUE_MARKER, "Object: {}, Value: {}", wrappedStack, EnergyValueRegistry.getInstance().getStackValueMap().get(wrappedStack));
                }
            }
        } else if (phase == EnergyValueRegistryProxy.Phase.ALL) {
            for (WrappedStack wrappedStack : EnergyValueRegistry.getInstance().getStackValueMap().keySet()) {
                LogHelper.info(ENERGY_VALUE_MARKER, "Object: {}, Value: {}", wrappedStack, EnergyValueRegistry.getInstance().getStackValueMap().get(wrappedStack));
            }
        }
        LogHelper.info(ENERGY_VALUE_MARKER, "END DUMPING {} ENERGY VALUE MAPPINGS", phase);
    }
}
