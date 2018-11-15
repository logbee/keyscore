package io.logbee.keyscore.test.fixtures

import io.logbee.keyscore.model.data._

object ExampleData {

  val messageTextField1 = Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C"))
  val messageTextField2 = Field("message", TextValue("Is is a rainy day. Temperature: 5.8 C"))
  val messageTextField3 = Field("message", TextValue("The weather is sunny with a current temperature of: 14.4 C"))

  val record1 = Record(messageTextField1)
  val record2 = Record(messageTextField2)
  val record3 = Record(messageTextField3)

  val multiFields1 = Record(
    Field("foo", TextValue("bar")),
    Field("bar", TextValue("42")),
    Field("bbq", TextValue("meat")),
    Field("beer", TextValue("non-alcoholic"))
  )

  val multiFields2 = Record(
    Field("foo", TextValue("example-foo")),
    Field("42", TextValue("example-bar"))
  )

  //CSV Filter
  val csvA = Record(Field("message", TextValue("13;07;09;15;;;")))
  val csvB = Record(Field("message", TextValue(";03;05;01;;;")))

  //Kafka
  val kafka1 = Record(
    Field("id", TextValue("01")),
    Field("name", TextValue("robo"))
  )
  val kafka2 = Record(
    Field("id", TextValue("02")),
    Field("name", TextValue("logbee"))
  )

  //Modified messages
  val record1Modified = Record(Field("weather-report", TextValue("cloudy, -11.5 C")))
  val record2Modified = Record(Field("weather-report", TextValue("rainy, 5.8 C")))
  val record3Modified = Record(Field("weather-report", TextValue("sunny, 14.4 C")))

  val multiRecordModified = Record(
    Field("bar", TextValue("42")),
    Field("bbq", TextValue("meat"))
  )
  val multiRecordModified2 = Record(
    Field("foo", TextValue("bar"))
  )

  //Original datasets
  val dataset1 = Dataset(MetaData(Label("name", TextValue("dataset1"))), record1)
  val dataset2 = Dataset(MetaData(Label("name", TextValue("dataset2"))), record2)
  val dataset3 = Dataset(MetaData(Label("name", TextValue("dataset3"))), record3)
  val dataset4 = Dataset(MetaData(Label("name", TextValue("dataset4"))), record1, multiFields1)
  val dataset5 = Dataset(MetaData(Label("name", TextValue("dataset5"))), record2, multiFields2)


  val datasetMulti1 = Dataset(multiFields1)
  val datasetMulti2 = Dataset(multiFields2)

  //CSV
  val csvDatasetA = Dataset(csvA)
  val csvDatasetB = Dataset(csvB)

  //Kafka
  val kafkaDataset1 = Dataset(kafka1)
  val kafkaDataset2 = Dataset(kafka2)

  //Modified datasets
  val dataset1Modified = Dataset(record1Modified)
  val dataset2Modified = Dataset(record2Modified)
  val dataset3Modified = Dataset(record3Modified)
  val datasetMultiModified = Dataset(multiRecordModified, multiRecordModified)
  val datasetMultiModified2 = Dataset(multiRecordModified2)

  //D3 BoxPlot
  val boxplot_html = "\n<head>\n  <meta charset=\"utf-8\">\n  <script src=\"https://d3js.org/d3.v4.min.js\"></script>\n  <style>\n    body { margin:0;position:fixed;top:0;right:0;bottom:0;left:0; }\n  </style>\n</head>\n\n<body>\n  <script>\n\n  var width = 900;\n  var height = 400;\n  var barWidth = 30;\n\n  var margin = {top: 20, right: 10, bottom: 20, left: 10};\n\n  var width = width - margin.left - margin.right,\n      height = height - margin.top - margin.bottom;\n\n  var totalWidth = width + margin.left + margin.right;\n  var totalheight = height + margin.top + margin.bottom;\n\n  //# # # # # # # # # # # # # # # # # # # # # # # # # # # #\n  // THESE ARE THE TWO PARAMETERS TO REPLACE IN THE BLOCK #\n  //# # # # # # # # # # # # # # # # # # # # # # # # # # # #\n\n  //#1 groupToListOfNumbers\nvar groupToListOfNumbers = {1:[14,17,19,12,15]}\n  \n  //#2 allNumbers\nvar allNumbers = [14,17,19,12,15]\n\n  // # # # # # # # # # # # # # # # # # #\n  // EVERYTHING ELSE CAN STAY THE SAME #\n  // # # # # # # # # # # # # # # # # # #\n\n\n  // Sort group numbers so quantile methods work\n  for(var key in groupToListOfNumbers) {\n    //Get List of numbers for the specified group\n    var listOfGroupNumbers = groupToListOfNumbers[key];\n    //Sort numbers\n    groupToListOfNumbers[key] = listOfGroupNumbers.sort(sortNumber);\n  }\n\n  // Setup a color scale for filling each box\n  var colorScale = d3.scaleOrdinal(d3.schemeCategory20)\n    .domain(Object.keys(groupToListOfNumbers));\n\n  // Prepare the data for the box plots\n  var boxPlotData = [];\n  // noinspection JSAnnotator\n  for (var [key, listOfGroupNumbers] of Object.entries(groupToListOfNumbers)) {\n\n    var record = {};\n    var localMin = d3.min(listOfGroupNumbers);\n    var localMax = d3.max(listOfGroupNumbers);\n\n    record[\"key\"] = key;\n    record[\"counts\"] = listOfGroupNumbers;\n    record[\"quartile\"] = boxQuartiles(listOfGroupNumbers);\n    record[\"whiskers\"] = [localMin, localMax];\n    record[\"color\"] = colorScale(key);\n\n    boxPlotData.push(record);\n  }\n\n  // Compute an ordinal xScale for the keys in boxPlotData\n  var xScale = d3.scalePoint()\n    .domain(Object.keys(groupToListOfNumbers))\n    .rangeRound([0, width])\n    .padding([0.5]);\n\n  // Compute a global y scale based on the global counts\n  var min = d3.min(allNumbers);\n  var max = d3.max(allNumbers);\n  var yScale = d3.scaleLinear()\n    .domain([min, max])\n    .range([0, height]);\n\n  // Setup the svg and the group we will draw the box plot in\n  var svg = d3.select(\"body\").append(\"svg\")\n    .attr(\"width\", totalWidth)\n    .attr(\"height\", totalheight)\n    .append(\"g\")\n    .attr(\"transform\", \"translate(\" + margin.left + \",\" + margin.top + \")\");\n\n  // Move the left axis over 25 pixels, and the top axis over 35 pixels\n  var axisG = svg.append(\"g\").attr(\"transform\", \"translate(25,0)\");\n  var axisTopG = svg.append(\"g\").attr(\"transform\", \"translate(35,0)\");\n\n  // Setup the group the box plot elements will render in\n  var g = svg.append(\"g\")\n    .attr(\"transform\", \"translate(20,5)\");\n\n  // Draw the box plot vertical lines\n  var verticalLines = g.selectAll(\".verticalLines\")\n    .data(boxPlotData)\n    .enter()\n    .append(\"line\")\n    .attr(\"x1\", function(datum) {\n        return xScale(datum.key) + barWidth/2;\n      }\n    )\n    .attr(\"y1\", function(datum) {\n        var whisker = datum.whiskers[0];\n        return yScale(whisker);\n      }\n    )\n    .attr(\"x2\", function(datum) {\n        return xScale(datum.key) + barWidth/2;\n      }\n    )\n    .attr(\"y2\", function(datum) {\n        var whisker = datum.whiskers[1];\n        return yScale(whisker);\n      }\n    )\n    .attr(\"stroke\", \"#000\")\n    .attr(\"stroke-width\", 1)\n    .attr(\"fill\", \"none\");\n\n  // Draw the boxes of the box plot, filled in white and on top of vertical lines\n  var rects = g.selectAll(\"rect\")\n    .data(boxPlotData)\n    .enter()\n    .append(\"rect\")\n    .attr(\"width\", barWidth)\n    .attr(\"height\", function(datum) {\n        var quartiles = datum.quartile;\n        var height = yScale(quartiles[2]) - yScale(quartiles[0]);\n        return height;\n      }\n    )\n    .attr(\"x\", function(datum) {\n        return xScale(datum.key);\n      }\n    )\n    .attr(\"y\", function(datum) {\n        return yScale(datum.quartile[0]);\n      }\n    )\n    .attr(\"fill\", function(datum) {\n      return datum.color;\n      }\n    )\n    .attr(\"stroke\", \"#000\")\n    .attr(\"stroke-width\", 1);\n\n  // Now render all the horizontal lines at once - the whiskers and the median\n  var horizontalLineConfigs = [\n    // Top whisker\n    {\n      x1: function(datum) { return xScale(datum.key) },\n      y1: function(datum) { return yScale(datum.whiskers[0]) },\n      x2: function(datum) { return xScale(datum.key) + barWidth },\n      y2: function(datum) { return yScale(datum.whiskers[0]) }\n    },\n    // Median line\n    {\n      x1: function(datum) { return xScale(datum.key) },\n      y1: function(datum) { return yScale(datum.quartile[1]) },\n      x2: function(datum) { return xScale(datum.key) + barWidth },\n      y2: function(datum) { return yScale(datum.quartile[1]) }\n    },\n    // Bottom whisker\n    {\n      x1: function(datum) { return xScale(datum.key) },\n      y1: function(datum) { return yScale(datum.whiskers[1]) },\n      x2: function(datum) { return xScale(datum.key) + barWidth },\n      y2: function(datum) { return yScale(datum.whiskers[1]) }\n    }\n  ];\n\n  for(var i=0; i < horizontalLineConfigs.length; i++) {\n    var lineConfig = horizontalLineConfigs[i];\n\n    // Draw the whiskers at the min for this series\n    var horizontalLine = g.selectAll(\".whiskers\")\n      .data(boxPlotData)\n      .enter()\n      .append(\"line\")\n      .attr(\"x1\", lineConfig.x1)\n      .attr(\"y1\", lineConfig.y1)\n      .attr(\"x2\", lineConfig.x2)\n      .attr(\"y2\", lineConfig.y2)\n      .attr(\"stroke\", \"#000\")\n      .attr(\"stroke-width\", 1)\n      .attr(\"fill\", \"none\");\n  }\n\n  // Setup a scale on the left\n  var axisLeft = d3.axisLeft(yScale);\n  axisG.append(\"g\")\n    .call(axisLeft);\n\n  // Setup a series axis on the top\n  var axisTop = d3.axisTop(xScale);\n  axisTopG.append(\"g\")\n    .call(axisTop);\n\n\tfunction boxQuartiles(d) {\n  \treturn [\n    \td3.quantile(d, .25),\n    \td3.quantile(d, .5),\n    \td3.quantile(d, .75)\n  \t];\n\t}\n    \n  // Perform a numeric sort on an array\n  function sortNumber(a,b) {\n    return a - b;\n  }\n\n  </script>\n</body>"

}
