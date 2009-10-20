Sets;

-- Sets that have nonempty intersection with (some of) their elements:
IMSets = Sets /^ [_s=s1] /^ Sets /^ [s1=m] ^ Sets;
IMSets;

-- Sets that have nonempty intersection with all their elements:
(IMSets /^ [_s=s1] /= Sets ^ [_s=s1]) v [_s];
