nnml: mbox.gz
	mkdir tmp-$@ 
	gzcat $< | (cd tmp-$@ && awk 'BEGIN {out="0.msg" } /From / {close(out); out=NR".M0"} {print > out }')
	mv tmp-$@ $@

mbox.gz: 
	curl http://lists.zs64.net/mailman-archive/cmucl-help/2010.txt.gz -o $@

clean:
	rm -r nnml/* mbox.gz
	rmdir nnml
