let user:string = "Eclipse User";
new Promise(resolve => setTimeout(resolve, 1000)).then(() => 
    console.log("Hello world,, " + user  + '!')
);