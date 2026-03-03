package mireka.imap;

import java.util.ArrayList;
import java.util.List;

import mireka.imap.parser.SeqRange;

public class SequenceSet {
	public List<SeqRange> ranges = new ArrayList<>();
	/** true if the set contains uids instead of message sequence numbers */
	public boolean uid;
	
	public List<SeqRange> expandLastResultMacro(Session session) {
		// TODO
		return ranges;
	}

	public void normalize(AsteriskSource as) {
		for (SeqRange r : ranges) {
			expandUidSeqRangeAsterix(r, as);
			order(r);
		}
	}
	
	private void expandUidSeqRangeAsterix(SeqRange r, AsteriskSource as) {
		if (r.begin == -1) {
			if (as.count == 0)
				r.begin = as.uidnext;
			else
				r.begin = as.lastuid;
		}
		if (r.end == -1) {
			if (as.count == 0)
				r.end = as.uidnext;
			else
				r.end = as.lastuid;
		}
	}
	private void order(SeqRange r) {
		if (r.end < r.begin) {
			long temp = r.begin;
			r.begin = r.end;
			r.end = temp;
		}
	}
	
        @Override
        public String toString() {
            return "SequenceSet [ranges=" + ranges + ", uid=" + uid + "]";
        }

        /**
         * Returns a {@link SequenceSet} which contains the supplied ids in the least number of
         * ranges.
         * 
         * @param ids ordered list of sequence numbers or uids.
         */
        public static SequenceSet fromIdList(List<Long> ids) {
            SequenceSet seq = new SequenceSet();
            seq.uid = true;
            // null means that no range is started now, the first uid in the list will be the start
            // of a range.
            SeqRange range = null;
            for (long uid : ids) {
                if (range == null) {
                    range = new SeqRange(uid);
                } else {
                    if (uid == range.end + 1) {
                        range.end++;
                    } else {
                        seq.ranges.add(range);
                        range = new SeqRange(uid);
                    }
                }
            }
            if (range != null)
                seq.ranges.add(range);
        
            return seq;
        }

        public void addToOrdered(long id) {
            SeqRange range;
            if (ranges.isEmpty()) {
                ranges.add(new SeqRange(id));
            } else {
                range = ranges.get(ranges.size() - 1);
                if (id == range.end + 1) {
                    range.end++;
                } else {
                    ranges.add(new SeqRange(id));
                }
            }
        }

        public static class AsteriskSource {
		public long count;
		public long uidnext;
		public long lastuid;

                @Override
                public String toString() {
                    return "AsteriskSource [count=" + count + ", uidnext=" + uidnext + ", lastuid="
                            + lastuid + "]";
                }
	}
}
