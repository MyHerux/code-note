# 使用Selenium破解拼图验证码

## 工具准备

- [官网](http://www.seleniumhq.org/)

- python 安装

  > pip install -U selenium

  or

  > python setup.py install

- 下载 chromedriver

  - [官网](http://chromedriver.storage.googleapis.com/index.html)

  - [chromedriver与chrome版本映射表](http://blog.csdn.net/huilan_same/article/details/51896672)

## 编码

- 使用 `Chromedriver` 打开目标网站

  ```
  path = ".\chromedriver.exe"
  driver = webdriver.Chrome(executable_path=path)
  url = "https://account.geetest.com/register"
  driver.get(url)
  time.sleep(5)
  ```
- 