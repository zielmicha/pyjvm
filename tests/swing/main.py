import reflect

JFrame = reflect.get_class('javax.swing.JFrame')
JButton = reflect.get_class('javax.swing.JButton')
ActionListener = reflect.get_class('java.awt.event.ActionListener')

def click(ev):
    print 'Hello world!'

f = JFrame.new()
f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)
button = JButton.new('Hello, world!')
button.addActionListener(reflect.implement(ActionListener, {'actionPerformed': click}))
f.add(button)
f.pack()
f.setVisible(True)
