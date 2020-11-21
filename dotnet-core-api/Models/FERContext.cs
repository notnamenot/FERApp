using Microsoft.Azure.CognitiveServices.Vision.Face;
using Microsoft.Azure.CognitiveServices.Vision.Face.Models;
using Microsoft.Azure.Cosmos;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Threading.Tasks;

namespace FERApi.Models
{
    public class FERContext
    {
        // ADD THIS PART TO YOUR CODE

        // The Azure Cosmos DB endpoint for running this sample.
        private static readonly string EndpointUri = "https://feraghdatabase.documents.azure.com:443/";
        // The primary key for the Azure Cosmos account.
        private static readonly string PrimaryKey = "My9j102mCD1cdexXsHGCG9D67xYDjKwpDS2Lxl8BJRTKH4MpDbInbLdNAUZIru0HEemVCXfxUsYyObw9qbC1GA==";


        const string SUBSCRIPTION_KEY = "d189946862284df0be6ed7faf568ab8c";
        const string ENDPOINT = "https://ferapifaceservice.cognitiveservices.azure.com/";
        readonly IFaceClient client;
        // The Cosmos client instance
        private CosmosClient cosmosClient;

        // The database we will create
        private Database database;

        // The container we will create.
        private Container labeledContainer;

        private Container unlabeledContainer;

        // The name of the database and container we will create
        private string databaseId = "FERDatabase";
        private string labeledContainerName = "FERLabeledContainer";
        private string unlabeledContainerName = "FERUnlabeledContainer";

        public FERContext()
        {
            try
            {
                Task.Run(() => GetStartedDemoAsync()).Wait();
            }
            catch (CosmosException de)
            {
                Exception baseException = de.GetBaseException();
                Console.WriteLine("{0} error occurred: {1}", de.StatusCode, de);
            }
            catch (Exception e)
            {
                Console.WriteLine("Error: {0}", e);
            }
            client = Authenticate(ENDPOINT, SUBSCRIPTION_KEY);
        }

        /// <summary>
        /// Create the database if it does not exist
        /// </summary>
        private async Task CreateDatabaseAsync()
        {
            // Create a new database
            this.database = await this.cosmosClient.CreateDatabaseIfNotExistsAsync(databaseId);
            Console.WriteLine("Created Database: {0}\n", this.database.Id);
        }

        public async Task GetStartedDemoAsync()
        {
            // Create a new instance of the Cosmos Client
            this.cosmosClient = new CosmosClient(EndpointUri, PrimaryKey);

            //ADD THIS PART TO YOUR CODE
            await this.CreateDatabaseAsync();
            //ADD THIS PART TO YOUR CODE
            await this.CreateContainersAsync();
        }

        private async Task CreateContainersAsync()
        {
            // Create a new container
            this.labeledContainer = await this.database.CreateContainerIfNotExistsAsync(labeledContainerName, "/id");
            Console.WriteLine("Created Container: {0}\n", this.labeledContainer.Id);

            this.unlabeledContainer = await this.database.CreateContainerIfNotExistsAsync(unlabeledContainerName, "/id");
            Console.WriteLine("Created Container: {0}\n", this.unlabeledContainer.Id);
        }

        public IFaceClient Authenticate(string endpoint, string key)
        {
            return new FaceClient(new ApiKeyServiceClientCredentials(key)) { Endpoint = endpoint };
        }

        public async Task AddElementToUnlabeledContainer(FERItem item)
        {
            try
            {
                ItemResponse<FERItem> readItemIfNotExisting = await this.unlabeledContainer.ReadItemAsync<FERItem>(item.Id, new PartitionKey(item.Id));
                Console.WriteLine("Item in database with id: {0} already exists\n", readItemIfNotExisting.Resource.Id);
            }
            catch (CosmosException ex) when (ex.StatusCode == HttpStatusCode.NotFound)
            {
                ItemResponse<FERItem> createNewItem = await this.unlabeledContainer.CreateItemAsync<FERItem>(item, new PartitionKey(item.Id));

                // Note that after creating the item, we can access the body of the item with the Resource property off the ItemResponse. We can also access the RequestCharge property to see the amount of RUs consumed on this request.
                Console.WriteLine("Created item in database with id: {0} Operation consumed {1} RUs.\n", createNewItem.Resource.Id, createNewItem.RequestCharge);
            }
        }

        public async Task AddElementToLabeledContainer(FERItem item)
        {
            try
            {
                ItemResponse<FERItem> readItemIfNotExisting = await this.labeledContainer.ReadItemAsync<FERItem>(item.Id, new PartitionKey(item.Id));
                Console.WriteLine("Item in database with id: {0} already exists\n", readItemIfNotExisting.Resource.Id);
            }
            catch (CosmosException ex) when (ex.StatusCode == HttpStatusCode.NotFound)
            {
                ItemResponse<FERItem> createNewItem = await this.labeledContainer.CreateItemAsync<FERItem>(item, new PartitionKey(item.Id));

                // Note that after creating the item, we can access the body of the item with the Resource property off the ItemResponse. We can also access the RequestCharge property to see the amount of RUs consumed on this request.
                Console.WriteLine("Created item in database with id: {0} Operation consumed {1} RUs.\n", createNewItem.Resource.Id, createNewItem.RequestCharge);
            }
        }

        internal async Task PostFERItem(FERItem ferItem)
        {
            try
            {

                // Detect - get features from faces.
                using MemoryStream ms = new MemoryStream(ferItem.Image);
                IList<DetectedFace> res = await client.Face.DetectWithStreamAsync(ms, false, false, new List<FaceAttributeType?> { FaceAttributeType.Emotion });
                var faceemotions = res[0].FaceAttributes.Emotion;
                switch ((Emotion)ferItem.EmotionID)
                {
                    case Emotion.Anger:
                        bool correct = CheckValueOfEmotion(faceemotions.Anger);
                        await AddElementToDB(ferItem, correct);
                        break;
                    case Emotion.Happiness:
                        correct = CheckValueOfEmotion(faceemotions.Happiness);
                        await AddElementToDB(ferItem, correct);
                        break;
                    case Emotion.Sadness:
                        correct = CheckValueOfEmotion(faceemotions.Sadness);
                        await AddElementToDB(ferItem, correct);
                        break;
                    case Emotion.Fear:
                        correct = CheckValueOfEmotion(faceemotions.Fear);
                        await AddElementToDB(ferItem, correct);
                        break;
                    case Emotion.Disgust:
                        correct = CheckValueOfEmotion(faceemotions.Disgust);
                        await AddElementToDB(ferItem, correct);
                        break;
                    case Emotion.Surprise:
                        correct = CheckValueOfEmotion(faceemotions.Surprise);
                        await AddElementToDB(ferItem, correct);
                        break;
                    case Emotion.Neutral:
                        correct = CheckValueOfEmotion(faceemotions.Neutral);
                        await AddElementToDB(ferItem, correct);
                        break;
                    default:
                        await AddElementToUnlabeledContainer(ferItem);
                        break;
                }
            }
            catch (Exception exception)
            {
                Console.WriteLine(exception.Message);
            }
        }

        private bool CheckValueOfEmotion(double emotion)
        {
            if (emotion > 0.7)
            {
                return true;
            }
            return false;
        }

        private async Task AddElementToDB(FERItem item, bool labeled = false)
        {
            if (labeled)
            {
                await AddElementToLabeledContainer(item);
            }
            else
            {
                await AddElementToUnlabeledContainer(item);
            }
        }

    }
}
