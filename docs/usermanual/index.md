---
title: "User Manual"
layout: single
toc: true
toc_label: "Table of content"
---

Welcome to the user manual for Money Manager Ex - Android, a free, open-source, cross-platform, and easy-to-use personal finance software.


# Introduction

Money Manager Ex - Android is a mobile version of the popular personal finance software Money Manager Ex. It allows you to manage your finances on the go, making it easy to track expenses, create budgets, and stay on top of your financial goals.

# Getting Started

To start using Money Manager Ex - Android, simply download and install the app from the Google Play Store or F-Droid. Once installed, you can create an account and begin adding your financial information.

You can also manually install from [github release page](https://github.com/moneymanagerex/android-money-manager-ex/releases/latest). 
Detail instruction [here](install.md).

Read [Quick Start Guide](/quickstart/) for initial documentation.

# Features

## Open Database Format

All information are stored in a sqllite [database](database.md).

## Expense Tracking

Track your expenses on the go, categorize transactions, and view detailed reports to better understand your spending habits.

## Budget Management ![Static Badge](https://img.shields.io/badge/since-5.2.3-green)
Set up [budgets](budget.md) for different categories and track your progress over time. Receive notifications when you're nearing your [budgets](budget.md) limits.
Monitor your [budgets](budget.md) consumption with your actual and forecast


## Account Management

Manage multiple accounts, including checking, savings, credit cards, and more. Keep track of balances and reconcile transactions with your bank statements.

## Reports

Generate customizable reports to gain insights into your financial health. Analyze spending patterns, income trends, and more.

### BuildIn Report

internal report allow you to see information regarding your expenses like:
- **Payee**: show amount spend for each payee
- **Where the money goes**: show amount for each expenses category 
- **Where the money comes**: show amount for each income category 
- **Category**: show amount for each category 
- **Income and Expenses**: show monthly income and expenses 
- **Cashflow**: this report show your future cashflow based on schedule transaction 

### General Report

**Notice** this function is partially supported. 
{: .notice--danger}

General report allow you to execute report loaded from [General Report Feature](https://moneymanagerex.org/docs/features/generalreports/)

Actually only execute SQL and HTML work. No LUA capability, and report need to be loaded from desktop version. There is actually no way to load from Android.


## Data Sync

Sync your financial data across multiple devices using cloud synchronization. Ensure that your information is always up to date, no matter where you are.

Read How to [setup companion app](/quickstart/start_companion/) for desktop.

## Security

Protect your financial information with advanced security features, including password protection and data encryption.

## Recurring Transactions

Support for recurring and schedule transactions. Recurring can be Manual, Prompt (with notifications) or Automatic (controlled by setting switch).

{% capture notice-2 %}
> Notice!**
> Expired Recurring Transaction require to have correct Type set into transaction
> AUTO: Means that transaction is auto posted when expired
> PROMPT: Means that transaction is show in a notification with action skip/enter for user decision
> MANUAL: Means that no action is taken on recurring transaction (no auto post, no notification)
{% endcapture %}
<div class="notice--warning">
  {{ notice-2 | markdownify }}
</div>

## Nested Category ![Static Badge](https://img.shields.io/badge/since-2024.08.25-green)

Money Manager Ex on Desktop can manage nested subcategory.
From 2024.08.25, nested category (means third level or more in category management) is available also on Android Version.
You can define (virtually) infinite subcategory level to categorize expenses. 

# How to Use

## Adding Transactions

To add a new transaction, simply navigate to the transactions tab and click on the add button. Then, enter the details of the transaction, including the date, amount, category, and any additional notes.

Detail instructions to how to enter transaction [here](add_transaction.md)

## Creating Budgets

To create a new budget, go to the budgets tab and click on the add button. Then, enter the details of the budget, including the category, amount, and timeframe.

## Managing Accounts

To manage your accounts, navigate to the accounts tab and click on the add button. Then, enter the details of the account, including the account type, name, and starting balance.

# Troubleshooting

If you encounter any issues while using Money Manager Ex - Android, please refer to the troubleshooting section of the user manual for assistance.

# Contributing

Money Manager Ex - Android is an open-source project, and we welcome contributions from the community. If you'd like to get involved, please visit our GitHub repository and submit a pull request.

# License

Money Manager Ex - Android is licensed under the GNU General Public License version 3.0. For more information, please refer to the [LICENSE](https://github.com/moneymanagerex/android-money-manager-ex?tab=GPL-3.0-1-ov-file) file in the root of the project repository.
