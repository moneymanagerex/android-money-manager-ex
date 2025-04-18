> [!CAUTION]
> This file is not used anymore

**Obsolete. Asset management is removed from 2023 series**

see https://github.com/moneymanagerex/android-money-manager-ex/commit/e64368d0f0588614fca268fee9e17c6c77312f5b 

# Asset Allocation

Asset Allocation is an important concept in investment. To use the Asset Allocation module in MoneyManagerEx (MMEX), you should know the basics of asset allocation and rebalancing, and have experience setting up and maintaining an asset allocation model. For some good articles on the topic see [Related links](#related) section below.

The benefits of Asset Allocation module in MMEX is that it provides instantaneous feedback about your asset allocation based on the currency exchange rates and stock prices.

To open Asset Allocation in MoneyManagerEx for Android, tap the Asset Allocation link in the drawer:

<img src="http://i.imgur.com/ixiAGzw.png" width="200px" />

This will open the main Asset Allocation screen:

<img src="http://i.imgur.com/TAO9h75.png" width="200px" />

Rotating the device into landscape mode will display the full version of the Asset Allocation:

<img src="http://i.imgur.com/Jjgwstv.png?1" width="400px" />

# Editing

For the purpose of the manual, we will use a simple allocation of 

- 60% stocks, of which
  - 30% international stocks (50% of the parent allocation)
  - 30% domestic stocks
- 37% bonds
- 3% cash

## Sub-Allocations

Each Asset Class can have sub-classes as shown for stocks in this case. 
When an asset class has sub-classes, its allocation and value will be calculated by adding together its sub-classes. Manually entered allocation value will not be used in this case.

## Editing Allocations

To add allocations, use the green plus button in the lower-right corner. The following Edit/Create screen will open.

<img src="http://i.imgur.com/kIGcHPe.png" width="200px" />

An allocation record simply needs a name and the allocation amount. The allocation amount is a simple number that represents the percentage of the total portfolio, like 30 for 30%.

## Cash

Cash asset class will be created automatically and can be edited later to set the allocation amount. At the moment, Cash asset class will use the cash (starting) amounts from investment accounts, converted to the base currency.

## Adding Stocks

Once an allocation is created and its desired percentage set, it is time to link stocks. Pressing the green plus button and then choosing Stocks,

<img src="http://i.imgur.com/1Q5gjEJ.png" width="200px" />

will display all non-assigned Stock symbols. Selecting a stock symbol will assign it to the current allocation class.

# Previewing

Asset Allocation can be previewed either by tapping the report icon while in Asset Allocation module, or by selecting it from the list of available Reports:

<img src="http://i.imgur.com/o3NdPhZ.png" width="200px" />

The preview will recalculate the allocations, the current values of asset classes, and the differences to the set allocations.
The parameters that affect the asset allocation are
- stock prices
- currency exchange values

Updating these will have an instant effect to your Asset Allocation preview.

# <a name="related">Related Links</a>

- [Asset Allocation](https://www.bogleheads.org/wiki/Asset_allocation) at Bogleheads
- [Vanguard portfolio allocation models](https://personal.vanguard.com/us/insights/saving-investing/model-portfolio-allocations)