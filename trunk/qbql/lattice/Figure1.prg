S1;
V;
S1V;

sv_p = S1V v [p];
sv_q = S1V v [q];
sv_pr = S1V v [p r];
sv_qr = S1V v [q r];

S1V /\ sv_p;
S1V v sv_qr;

S1V v sv_p;
S1V /\ sv_qr;

S1V /\ sv_q;
S1V v sv_pr;

S1V v sv_q;
S1V /\ sv_pr;
