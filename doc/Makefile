RM = /bin/rm
TEX = /usr/local/texlive/2014/bin/x86_64-darwin/pdflatex
BIB = /usr/local/texlive/2014/bin/x86_64-darwin/bibtex

all: felix_report.pdf

felix_report.aux: felix_report.tex references.bib
	$(TEX) felix_report.tex
	$(BIB) felix_report.aux

felix_report.pdf: felix_report.aux
	$(TEX) felix_report.tex
	$(TEX) felix_report.tex

clean:
	$(RM) -f *{out,bbl,blg,dvi,log,aux,hst,ver,toc,lof,lot,upa,upb,pdf}

