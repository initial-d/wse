1. beta for linear

I'm not using simply linear, since the Value of QL, numviews and phrase score are varied too much, I compute log2 for these values to combine the together.

The final equation is 

linear_score = beta1(1.0)*cosine_score +
               beta2(0.1)* log2(QL_score) +
               beta3(0.1)* log2(phrase_score) +
               beta4(0.05)* log2(numviews_score)

2.bonus:

use following instruction to simulate a click:

curl "localhost:25809/click?did=<did>&sid=<sessionID>"

did is the document id
SessionID is give by server associate with a query. SessionID has to be associate with a previous query, otherwise the click will be ignored.
