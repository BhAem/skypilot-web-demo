import os
import sky

from flask import Flask, render_template

app = Flask(__name__)

parentFolderPath = "D:\\111" # 父文件夹路径 D:\111  /home/wuqingfu
folderName = "user001-workdir" # 要创建的文件夹名称
yamlName = "example.yaml"
pythonName = "skypilot_test.py"

@app.route('/')
def hello_world():
   return render_template('index.html')

@app.route('/project/mkdir',methods=['PUT'])
def mkdir():
    path = os.path.join(parentFolderPath, folderName)
    os.mkdir(path)
    return "success"

@app.route('/project/runTask',methods=['PUT'])
def runCMD():
    task = sky.Task.from_yaml('/home/wuqingfu/user001-workdir/example.yaml')
    sky.launch(task, cluster_name='mycluster')
    return "success"


if __name__ == '__main__':
    app.run()