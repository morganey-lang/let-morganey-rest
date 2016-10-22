
## Rest-API


#### /
**Method**: GET  
**Description**: Returns the `let-morganey-rest` webpage.  
**Response**: HTML

--

#### /evaluate
**Method**: POST  
**Description**: Evaluates a lambda term, which has to be provided as a json object in the body of the POST request. The json object has to have the form: 
```
{
    term : String
}
```
**Response**: JSON-object of the form:
```
{ 
    error    : Boolean,
    messages : [String]
}
```
If `error` is true, an error occured while parsing the term. In this case `messages` contains at least one error message. If `error` is false, `messages` contains the result of the evaluation.  

--

#### /autocomplete
**Method**: POST  
**Description**: Autocomplete a term, which was typed in partially. The term has to be provided as a json object in the body of the POST request. It has to have the form:
```
{
    line : String
}
```
**Response**: JSON-object of the form:
```
{ 
    error    : Boolean,
    messages : [String]
}
```
If `error` is true, an error occured while parsing the term. In this case `messages` contains at least one error message. If `error` is false, `messages` contains autocompletion proposals.  

