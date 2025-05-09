// Basic IT: index is created and contains one class FQN

File index = new File(basedir, 'target/classes/META-INF/sisu/javax.inject.Named');
assert index.isFile()
assert index.text.contains('simple.Test')

