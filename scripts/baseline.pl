#!/usr/bin/perl -w

use strict;

my@doc=<STDIN>;
chomp(@doc);
my$doc=join(" ",@doc); # one line, return characters transformed into spaces

# clean up header
if($doc=~ m/RÉSOLU À L'UNANIMITÉ\:/){
    $doc =~ s/.*RÉSOLU À L'UNANIMITÉ://;
}

my@amounts = $doc =~ m/(\d?\d?(?:\s?|\.?)\d{3}(?:\s?|,?)(?:\d{3})?(?:\,\d{2})?\s?\$)/g;
if(!@amounts){ # no amount, bail-out
    print "0\n"; exit;
}

# has amount
if($#amounts){ # more than one amount, bail-out
    print "0\n"; exit;
}
# got the amount, now find the company
my$amount = pop @amounts;
    
# is there a inc. ?
my@incs = split(/\sinc\./i, $doc);
if(!$#incs){ # no company, bail-out
    print "0\n"; exit;
}
    
# let's focus on the first one, we know $incs[0] ends in \sinc.
my$company = $incs[0];
# trim as much as possible
$company=~ s/.*firme//i;
$company=~s/.*à//i;
$company=~s/.*\spar\sle//i;
$company=~s/.*\sla\scompagnie\s//i;

# reason is a crapshoot
my$reason="";
if($doc =~ m/((du\scontrat\sde)|(requis\spour)|(concernant)|(\Q$amount\E\spour)|(\Q$company\E\s[Ii][Nn][Cc]\.\spour))/){
    ($reason) = $doc =~ m/(?:(?:du\scontrat\sde)|(?:requis\spour)|(?:concernant)|(?:\Q$amount\E\spour)|(?:\Q$company\E\s[Ii][Nn][Cc]\.\spour))\s(.*)/;
    # trim aggressively
    $reason =~ s/(\,|\.|\;).*//;
}

print "1\t$amount\t$company inc.\t$reason\n";
