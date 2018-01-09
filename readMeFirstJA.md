# SVG Map Toolsについて

SVG Map Toolsは、Shapefileや緯度経度を持ったCSVファイルからSVGMapコンテンツを生成するツールです。
Javaで構築されており、Java(Oracle版)が動作する環境が必要です。主にWindowsおよびCentOSで動作確認しています。

コンテンツは、データの密度に応じて、ラスターとベクターを混合した四分木タイルとしてとして生成することができます。
これにより、[この例のような](http://svgmap.org/devinfo/devkddi/lvl0.1/rev14/SVGMapper_r14.html#visibleLayer=worldcities&hiddenLayer=polygonAuthoringTester)数百万件以上のデータの地図コンテンツを構築できます。


現在のところ以下の４つのモジュールで構成されています。このうちShape2SVGMapとShape2ImageSVGMapが基本となるモジュールです。

SVGMapの概要・どのようなコンテンツが表示できるかは、http://svgmap.org/ を参照ください。

* Shape2SVGMap: CSVもしくはShapefile形式のデータを、ベクトルデータのSVGMapコンテンツに変換します。
* Shape2ImageSVGMap: CSVもしくはShapefile形式のデータを、ビットイメージのSVGMapコンテンツに変換します。
* Shape2WGS84:  測地系変換や属性に応じたファイル分割、Shapefile<>CSV変換などを行うプリプロセッサ
* HyperBuilder: 複数のSVGMapレイヤーを統合するコンテナデータを生成

先ず、[tutorial](tutorial)ディレクトリの内容を用いて、基本モジュール（Shape2SVGMap、Shape2ImageSVGMap）の基本操作を学習し、処理の流れを理解してから使用してください。なお、tutorialsの内容はWindowsように調整されています。Linuxなどで使う場合は適宜翻訳して利用してください。

環境の設定
このファイルがあるディレクトリをカレントディレクトリとし、その配下のlibsディレクトリに、本ツールが使用するクラスライブラリ、[geotools2.7.5](https://sourceforge.net/projects/geotools/files/GeoTools%202.7%20Releases/2.7.5/) と [javacsv2.1](https://sourceforge.net/projects/javacsv/)　のjarファイル群を配置する必要があります。javacsvは一つのjarファイル、geotoolsは大量のjarファイルから構成されています。いずれもオープンソースソフトウェアです。以下からそれぞれダウンロードできます。

Shape2WGS84を用いて、日本測地系(TOKYO Datum)から世界測地系への測地系変換を行いたい場合、カレントディレクトリに国土地理院が公開する変換パラメータデータ["TKY2JGD.par"(Ver2.1.2)](http://www.gsi.go.jp/sokuchikijun/tky2jgd_download.html)　をこのファイル名で配置してください。

## ツールの起動方法(Windows)
ツールはカレントディレクトリで以下のコマンドにより起動できます。オプションなしで起動するとヘルプが表示されます。
環境の設定方法は[tutorial](tutorial)に解説があります。まずは、[releases](https://github.com/svgmap/svgMapTools/releases)から、チュートリアルが含まれたソースとコンパイル済みのjarファイルをダウンロードして環境を準備してください。

* `java -classpath lib\*;shape2svgmap.jar Shape2SVGMap (options)`
* `java -classpath lib\*;shape2svgmap.jar Shape2ImageSVGMap (options)`
* `java -classpath lib\*;shape2svgmap.jar Shape2WGS84 (options)`
* `java -classpath lib\*;shape2svgmap.jar HyperBuilder (options)`


ライセンスについて
本ツールは[GPL Ver.3](LICENSE)に基づくオープンソースソフトウェアです。