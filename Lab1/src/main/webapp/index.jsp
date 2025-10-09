<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Choose Your Favorite Animal</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<div class="container">
    <h1>Choose Your Favorite Animal</h1>

    <form action="ControllerServlet" method="get" class="animal-form">
        <div class="animal-option">
            <img src="https://png.pngtree.com/png-vector/20241123/ourmid/pngtree-adorable-golden-cat-clipart-standing-illustration-png-image_14489824.png" alt="Cat" class="animal-img">
            <label>
                <input type="radio" name="page" value="cat" required>
                Cat
            </label>
        </div>

        <div class="animal-option">
            <img src="https://png.pngtree.com/png-clipart/20250118/original/pngtree-golden-retriever-dog-pictures-png-image_20183713.png" alt="Dog" class="animal-img">
            <label>
                <input type="radio" name="page" value="dog">
                Dog
            </label>
        </div>

        <button type="submit" class="btn">Show me the page!</button>
    </form>
</div>
</body>
</html>
