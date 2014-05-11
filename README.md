
This module was made for quickly integration one click pay library from Privat Bank Ukraine (https://m.privatbank.ua/andrapi/). Privat Bank only represents methods for work with they system and don't allow integrate they library in several rows. So I've added dialogs for intergate one click pay library more easier.

Module <b>Payment</b> is a example project.

For using this library you just have to add module "<b>Privatbank</b>" in your project and addd following lines into code:

  //Create field in your actitvity<br>
  <i>private OneClickPayIntegrator mIntegrator;</i>

  //Init field <br>
  <i>mIntegrator = new OneClickPayIntegrator(this, MERCHANT_ID);</i>

  //Invoke lines bellow for make payment<br>
  <i>PayData payData = new PayData();<br>
  payData.setCcy(OneClickPayIntegrator.Currency.UAH.name());<br>
  payData.setDescription(String.format(DESCRIPTION));<br>
  payData.setAmount("0.01");<br>
  mIntegrator.pay(payData);<br></i>
