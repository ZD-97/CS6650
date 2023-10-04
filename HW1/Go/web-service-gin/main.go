package main

import (
	"fmt"
	"io/ioutil"
	"net/http"

	"github.com/gin-gonic/gin"
)

type album struct {
	ID     string  `json:"id"`
	Title  string  `json:"title"`
	Artist string  `json:"artist"`
	Price  float64 `json:"price"`
}

type ErrorMsg struct {
	Msg string `json:"msg"`
}

type Response struct {
	AlbumID   string `json:"albumID"`
	ImageSize string `json:"imageSize"`
}

//	var albums = []album{
//		{ID: "1", Title: "Blue Train", Artist: "John Coltrane", Price: 56.99},
//		{ID: "2", Title: "Jeru", Artist: "Gerry Mulligan", Price: 17.99},
//		{ID: "3", Title: "Sarah Vaughan and Clifford Brown", Artist: "Sarah Vaughan", Price: 39.99},
//	}
var myAlbum album = album{
	ID:     "1",
	Title:  "Blue Train",
	Artist: "John Coltrane",
	Price:  56.99,
}

func main() {
	router := gin.Default()
	router.GET("/albums", getAlbums)
	router.POST("/albums", postAlbums)
	router.GET("/albums/:id", getAlbums)
	router.Run(":8080")
}

func getAlbums(c *gin.Context) {
	c.IndentedJSON(http.StatusOK, myAlbum)
}

func postAlbums(c *gin.Context) {
	image, err := ioutil.ReadAll(c.Request.Body)
	if err != nil {
		badresp := ErrorMsg{
			Msg: "An error occurred",
		}
		c.JSON(http.StatusBadRequest, badresp)
		return
	}

	// Calculate the image size
	imageSize := len(image)

	// Prepare the response
	resp := Response{
		AlbumID:   "123", // Replace with actual Album ID
		ImageSize: fmt.Sprintf("%d bytes", imageSize),
	}

	// Send the JSON response
	c.IndentedJSON(http.StatusOK, resp)
}

// func getAlbumByID(c *gin.Context) {
// 	id := c.Param("id")

// 	// Loop over the list of albums, looking for
// 	// an album whose ID value matches the parameter.
// 	for _, a := range albums {
// 		if a.ID == id {
// 			c.IndentedJSON(http.StatusOK, a)
// 			return
// 		}
// 	}
// 	c.IndentedJSON(http.StatusNotFound, gin.H{"message": "album not found"})
// }
