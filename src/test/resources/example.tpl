<!-- #section header || other -->
~$firstName~ ~$lastName:'upper~                                        Bordeaux, ~$date:'date_formatter<'eeddmmyyyy[]>:'titleize~
~|rt|$streetLines#for<'streetLine>~
    ~|lt|$streetLine:'titleize~
~|rt|#for~
~$zip~ ~$city:'upper~
~$country!"FRANCE"¡~
~'email[$email?]~

~'client_number[$clientNumber]~
~'account_number[$accountNumber!'unknown[]¡]~
~'line_number[$lineNumber!'unknown[]¡]~
~'client_since[$inscription?:'date_formatter<'ddmmyyyy[]>]~

<!-- #section body -->

~$genre:'gender<"Madame","Monsieur">~,

Veuillez trouver la facture relative à votre ligne.

~'you_have[$options:'size]~
~|rt|$options#for<'option>~
    ~|lt|$option:'toModel#override~  - ~|rt|~
        ~|lt|$label~: ~'price[$price]~ (~$unit[$quantity]~)
    ~|lt,rt|#override~
~|lt|#for~
~$totaux:'toModel#override~~$label~: ~'price[$price]~~#override~


