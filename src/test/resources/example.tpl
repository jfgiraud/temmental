<!-- #section header -->
~$firstName~ ~$lastName:'upper~                                        Bordeaux, ~$date:'date_formatter<'eeddmmyyyy[]>:'titleize~
~$streetLines#for<'streetLine>~~$streetLine:'titleize~
~#for~~$zip~ ~$city:'upper~
~$country!"FRANCE"¡~
~'email[$email?]~

~'client_number[$clientNumber]~
~'account_number[$accountNumber!'unknown[]¡]~
~'line_number[$lineNumber!'unknown[]¡]~
~'client_since[$inscription?:'date_formatter<'ddmmyyyy[]>]~

<!-- #section body -->
