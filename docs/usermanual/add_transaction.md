# Transactions

- [Account Transactions List](account_transactions_list.md)
- [Reconciling and Balancing Transactions](reconcile_transaction.md)

What is an Transaction in <a href="/">MoneyManagerEx for Android</a>? A transaction is any money movement, including transfers, withdrawals, and deposits.

To quickly enter a transaction, you can tap the Add Transaction (+) button in the main screen. 
To edit and view transactions for an account, tap the desired account on the main screen. Once the account transactions are listed, click the menu Add Transaction (+) to open the Add/Edit Transaction activity. 


Enter the details:

- <strong>Date</strong>: This is generally the date of the transaction. Defaults to current day and can be changed.
- <strong>Status</strong>:
  - <strong>Unreconciled</strong>: When you enter a transaction, it initially is in the state of "Unreconciled". Which means the transaction has not been reconciled with your bank/credit card company's balance.
  - <strong>Reconciled</strong>: Once the transaction is checked and verified with a credit card company's balance information, it can be marked as reconciled.
  - <strong>Void</strong>: If you entered a transaction that later became invalid or you canceled the transaction, instead of deleting the transaction you can also mark it as void so you have a record of the transaction.
  - <strong>Followup</strong>: This status marks transactions as needing more action. For example, you receive a balance statement from the financial institution and you notice that the transaction amount is different between what you recorded and what is in the statement. You can mark it as flag for follow up so that you can followup with the financial institution.
  - <strong>Duplicate</strong>: This status marks transactions as duplicate.
- <strong>Type</strong>:
  - <strong>Withdrawal</strong>: is one where one makes a payment and is an expense.</li>
  - <strong>Deposit</strong>: is one where money is received and is an income.</li>
  - <strong>Transfer</strong>: is one where a withdrawal is made from one account and is deposited into another account. This type of transaction is not included in Income/Expense calculations.</li>
- <strong>Amount:</strong> Enter the amount for the transaction.
- <strong>Payee</strong>: This is a subject (person, company or organization) to whom the money goes or comes from. Clicking the payee button opens up the Payee activity. You can select the payee from that list or create a new payee (+) for immediate use.</li>
- <strong>Category</strong>: Category specifies the kind of expense/income for the transaction. As for the payee by clicking the categories button, you can select a category or create a new one immediately.</li>
- <strong>Transaction Number</strong> and <strong>Note</strong>: is additional information that you can enter in the transaction</li>
    
Finally, press "Ok" button to save. If you want to abort the transaction, press the "Abort"

If you want to edit, delete or modify a transaction was a transaction hold over the desired transaction. Then it will open a context menu with the possible operations.

### <a name="Reconciling_and_Balancing_Transactions"></a>Reconciling and Balancing Transactions

- Unreconciled Transactions: means that transactions have not been verified with the statement from the financial institution.
- Reconciled Transactions: A transaction can be considered reconciled when the details of the transaction match that from the financial institution.

