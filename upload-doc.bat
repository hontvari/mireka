ant doc
svn delete https://mireka.googlecode.com/svn/doc -m "public doc is removed before update"
svn import --auto-props build/doc https://mireka.googlecode.com/svn/doc -m "public doc is updated"

