import express from "express";

const port = process.env.PORT ?? 5000;

const router = express.Router();

router.all("*", (req, res) => {
  console.log({
    url: req.url,
    method: req.method,
    headers: req.headers,
  });

  console.log('BODY', req.body);

  res.json({
    message: "OK",
  });
});

express()
  .use(express.json())
  .use(router)
  .listen(
    port,
    console.info(`Server running ${process.env.NODE_ENV} mode on port ${port}`)
  );
